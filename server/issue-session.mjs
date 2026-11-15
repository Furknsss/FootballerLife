import { issueSession } from './dialogue-server.mjs';
// Run only on the trusted server; never ship the signing secret in the Android app.
console.log(issueSession(process.argv[2], process.env.DIALOGUE_SESSION_SECRET));
