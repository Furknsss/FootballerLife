import { test } from 'node:test';
import assert from 'node:assert/strict';
import { issueSession, verifySession, validateReply, makeServer } from './dialogue-server.mjs';

const secret = 'test-only-session-signing-secret-with-32-characters';
const valid = { dialogue: 'Kadro kararını performansına göre değerlendireceğim.', emotion: 'Ciddi', tone: 'Profesyonel', intent: 'CONVERSATION', proposedAction: null, targetEntityId: null, memoryCandidate: null, conversationStatus: 'OPEN' };
const context = { npc: { id: 'NPC_MANAGER', role: 'Teknik Direktör' }, gameDay: 1, message: 'Hocam daha fazla süre istiyorum.' };

async function withServer(options, run) {
  const server = makeServer(options);
  await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
  try { await run(`http://127.0.0.1:${server.address().port}/dialogue`); }
  finally { server.closeAllConnections(); await new Promise(resolve => server.close(resolve)); }
}

test('sessions expire and forged signatures are rejected', () => {
  const now = 1_800_000_000_000;
  const token = issueSession('player-1', secret, now);
  assert.equal(verifySession(token, secret, now), 'player-1');
  assert.equal(verifySession(token, secret, now + 3_600_001), null);
  assert.equal(verifySession(token + 'forged', secret, now), null);
});

test('model cannot propose economic or career mutations', () => {
  assert.equal(validateReply(valid), true);
  assert.equal(validateReply({ ...valid, proposedAction: 'PAY_SIGNING_BONUS' }), false);
  assert.equal(validateReply({ ...valid, targetEntityId: 'CLUB' }), false);
  assert.equal(validateReply({ ...valid, dialogue: '' }), false);
});

test('missing AI configuration returns unavailable, not fake dialogue', async () => {
  await withServer({ env: { DIALOGUE_SESSION_SECRET: secret } }, async url => {
    const unauthorized = await fetch(url, { method: 'POST' });
    assert.equal(unauthorized.status, 401);
    const unavailable = await fetch(url, { method: 'POST', headers: { Authorization: `Bearer ${issueSession('player', secret)}` } });
    assert.equal(unavailable.status, 503);
  });
});

test('validated structured response uses the server key and no allowed actions', async () => {
  let request;
  await withServer({ env: { DIALOGUE_SESSION_SECRET: secret, GEMINI_API_KEY: 'test-key-only', GEMINI_MODEL: 'configured-test-model' },
    fetchImpl: async (url, options) => { request = { url, options }; return { ok: true, json: async () => ({ candidates: [{ content: { parts: [{ text: JSON.stringify(valid) }] } }] }) }; } }, async url => {
    const response = await fetch(url, { method: 'POST', headers: { Authorization: `Bearer ${issueSession('player', secret)}`, 'Content-Type': 'application/json' }, body: JSON.stringify({ ...context, allowedActions: ['TRANSFER_COMPLETED'] }) });
    assert.equal(response.status, 200);
    assert.deepEqual(await response.json(), valid);
    assert.equal(request.options.headers['x-goog-api-key'], 'test-key-only');
    const upstream = JSON.parse(request.options.body);
    assert.deepEqual(JSON.parse(upstream.contents[0].parts[0].text).allowedActions, []);
  });
});

test('unsafe model response and network failure fail closed', async () => {
  for (const fetchImpl of [async () => { throw new Error('offline'); }, async () => ({ ok: true, json: async () => ({ candidates: [{ content: { parts: [{ text: JSON.stringify({ ...valid, proposedAction: 'TRANSFER_COMPLETED' }) }] } }] }) })]) {
    await withServer({ env: { DIALOGUE_SESSION_SECRET: secret, GEMINI_API_KEY: 'test-key-only', GEMINI_MODEL: 'test' }, fetchImpl }, async url => {
      const result = await fetch(url, { method: 'POST', headers: { Authorization: `Bearer ${issueSession('player', secret)}`, 'Content-Type': 'application/json' }, body: JSON.stringify(context) });
      assert.ok([502, 503].includes(result.status));
    });
  }
});

test('request quota prevents unbounded AI calls', async () => {
  await withServer({ env: { DIALOGUE_SESSION_SECRET: secret, GEMINI_API_KEY: 'test-key-only', GEMINI_MODEL: 'test' }, fetchImpl: async () => ({ ok: false }) }, async url => {
    const headers = { Authorization: `Bearer ${issueSession('player', secret)}`, 'Content-Type': 'application/json' };
    for (let i = 0; i < 12; i++) await fetch(url, { method: 'POST', headers, body: JSON.stringify(context) });
    assert.equal((await fetch(url, { method: 'POST', headers, body: JSON.stringify(context) })).status, 429);
  });
});
