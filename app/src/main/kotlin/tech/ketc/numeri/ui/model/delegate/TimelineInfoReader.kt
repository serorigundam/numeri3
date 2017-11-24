package tech.ketc.numeri.ui.model.delegate

import tech.ketc.numeri.App
import tech.ketc.numeri.R
import tech.ketc.numeri.domain.repository.ITimelineInfoRepository
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.domain.twitter.model.UserList
import tech.ketc.numeri.infra.element.TlType
import tech.ketc.numeri.infra.entity.TimelineGroup
import tech.ketc.numeri.infra.entity.TimelineInfo
import tech.ketc.numeri.util.arch.coroutine.asyncResponse

class TimelineInfoReader(private val mApp: App, private val mRepository: ITimelineInfoRepository) : ITimelineInfoReader {
    override fun loadGroupList() = asyncResponse { mRepository.getGroupList() }

    override fun loadTimelineInfoList(groupName: String)
            = asyncResponse { mRepository.selectByGroup(TimelineGroup(groupName)) }

    override fun toName(info: TimelineInfo, clientUsers: List<Pair<TwitterClient, TwitterUser>>, userListMap: Map<TwitterClient, List<UserList>>): String {
        fun TimelineInfo.clientToUser() = clientUsers.find { it.first.id == this.accountId }!!
        fun TimelineInfo.user() = clientToUser().second
        fun TimelineInfo.client() = clientToUser().first
        fun TimelineInfo.listName() = userListMap[client()]!!.find { it.id == foreignId }?.name

        return when (info.type) {
            TlType.HOME -> "Home:${info.user().screenName}"
            TlType.MENTIONS -> "Mentions:${info.user().screenName}"
            TlType.USER_LIST -> {
                val listName = info.listName()
                if (listName == null) mApp.getString(R.string.deleted_list) else "List:" + listName
            }
            TlType.PUBLIC -> ""
            TlType.FAVORITE -> ""
        }
    }
}