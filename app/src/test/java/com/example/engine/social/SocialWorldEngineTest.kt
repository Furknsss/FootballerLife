package com.example.engine.social

import com.example.engine.CareerEngine
import com.example.engine.WorldDataCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SocialWorldEngineTest {

    private fun createTestCareer(followerCount: Int = 500, overall: Int = 50) =
        CareerEngine.createNewCareerFromRealPreset(
            slotId = 1,
            preset = WorldDataCatalog.realPlayerPresets.first()
        ).let { state ->
            state.copy(
                player = state.player.copy(
                    followersCount = followerCount,
                    overallRating = overall
                )
            )
        }

    @Test
    fun test1_playerFameTierScaling() {
        val amateur = createTestCareer(followerCount = 150, overall = 48)
        assertEquals(PlayerFameTier.AMATEUR, SocialWorldGenerator.calculatePlayerFameTier(amateur))

        val semiPro = createTestCareer(followerCount = 5_000, overall = 58)
        assertEquals(PlayerFameTier.SEMI_PRO, SocialWorldGenerator.calculatePlayerFameTier(semiPro))

        val rising = createTestCareer(followerCount = 35_000, overall = 68)
        assertEquals(PlayerFameTier.RISING_STAR, SocialWorldGenerator.calculatePlayerFameTier(rising))

        val superLig = createTestCareer(followerCount = 200_000, overall = 76)
        assertEquals(PlayerFameTier.SUPER_LIG_STAR, SocialWorldGenerator.calculatePlayerFameTier(superLig))

        val national = createTestCareer(followerCount = 1_200_000, overall = 82)
        assertEquals(PlayerFameTier.NATIONAL_HERO, SocialWorldGenerator.calculatePlayerFameTier(national))

        val superstar = createTestCareer(followerCount = 10_000_000, overall = 88)
        assertEquals(PlayerFameTier.GLOBAL_SUPERSTAR, SocialWorldGenerator.calculatePlayerFameTier(superstar))
    }

    @Test
    fun test2_commentLikesScaleWithFame() {
        val amateur = createTestCareer(followerCount = 100, overall = 50)
        val amateurLikes = SocialWorldGenerator.calculatePlayerCommentLikes(amateur)
        assertTrue("Amateur likes should be between 18 and 95, was $amateurLikes", amateurLikes in 18..95)

        val superstar = createTestCareer(followerCount = 5_000_000, overall = 89)
        val starLikes = SocialWorldGenerator.calculatePlayerCommentLikes(superstar)
        assertTrue("Superstar likes should be >= 350,000, was $starLikes", starLikes >= 350_000)

        // Test dynamic comment like growth
        val initialLikes = starLikes
        val grownLikes = SocialWorldGenerator.growCommentEngagement("CMT_TEST_1", superstar)
        assertTrue(grownLikes > initialLikes)
    }

    @Test
    fun test3_userCommentGeneratesMultiPersonaReplies() {
        val state = createTestCareer(followerCount = 50_000, overall = 70)
        val posts = SocialWorldGenerator.getFeedPosts(state)
        assertTrue(posts.isNotEmpty())
        val targetPost = posts.first()

        SocialWorldGenerator.addInstaComment(
            postId = targetPost.id,
            userText = "Bu maçı kazanmak için her şeyimizi verdik!",
            state = state
        )

        val comments = SocialWorldGenerator.getPostComments(targetPost.id, state)
        val userComment = comments.find { it.isUserComment }
        assertNotNull("User comment must be registered", userComment)

        val replies = SocialWorldGenerator.getCommentReplies(userComment!!.id, targetPost.id, state)
        assertTrue("User comment must trigger replies, got ${replies.size}", replies.isNotEmpty())

        val reactionTypes = replies.mapNotNull { it.reactionType }.toSet()
        assertTrue("Should include fan support", reactionTypes.contains(CommentReactionType.FAN_SUPPORT))
        assertTrue("Should include rival troll", reactionTypes.contains(CommentReactionType.RIVAL_TROLL))
        assertTrue("Should include meme culture", reactionTypes.contains(CommentReactionType.MEME_CULTURE))
    }

    @Test
    fun test4_infiniteScrollAndRefresh() {
        val state = createTestCareer()
        val initialPosts = SocialWorldGenerator.getFeedPosts(state)
        val initialCount = initialPosts.size

        // Load more posts (infinite scroll pagination)
        val loadedCount = SocialWorldGenerator.loadMoreInstaPosts(state)
        assertTrue("loadMore should generate new posts", loadedCount > 0)
        val afterLoadCount = SocialWorldGenerator.getFeedPosts(state).size
        assertTrue(afterLoadCount > initialCount)

        // Pull to refresh
        val refreshCount = SocialWorldGenerator.refreshInstaFeed(state)
        assertTrue("refresh should prepend breaking posts", refreshCount > 0)
        val afterRefreshCount = SocialWorldGenerator.getFeedPosts(state).size
        assertTrue(afterRefreshCount > afterLoadCount)
    }

    @Test
    fun test5_diverseNonPlayerContentExists() {
        val state = createTestCareer()
        val posts = SocialWorldGenerator.getFeedPosts(state)
        val tweets = SocialWorldGenerator.getXPosts(state)

        val allContent = (posts.map { it.content } + tweets.map { it.text }).joinToString(" ")

        // Check Champions League / European giants
        assertTrue("Should contain Champions League or European football",
            allContent.contains("Champions League") || allContent.contains("City") || allContent.contains("Madrid") || allContent.contains("Arsenal"))

        // Check Turkish football banter / culture
        assertTrue("Should contain Turkish league banter",
            allContent.contains("derbi") || allContent.contains("taraftar") || allContent.contains("Süper Lig") || allContent.contains("Eskişehirspor"))

        // Check VAR / Referee discussions
        assertTrue("Should contain VAR / referee controversies",
            allContent.contains("VAR") || allContent.contains("Trio") || allContent.contains("hakem") || allContent.contains("penaltı"))

        // Check Transfer rumors / Romano
        assertTrue("Should contain transfer rumors",
            allContent.contains("Here we go") || allContent.contains("transfer") || allContent.contains("Romano") || allContent.contains("Sabuncuoğlu"))
    }
}
