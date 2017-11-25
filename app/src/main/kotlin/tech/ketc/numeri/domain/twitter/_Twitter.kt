package tech.ketc.numeri.domain.twitter

import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.Tweet

infix fun Tweet.isMention(client: TwitterClient) = userMentionEntities.any { it.id == client.id }
        && retweetedTweet == null