package tech.ketc.numeri.infra.dao

import android.arch.persistence.room.*
import tech.ketc.numeri.infra.element.TlType
import tech.ketc.numeri.infra.entity.TimelineInfo
import tech.ketc.numeri.infra.entity.TlGroupToTlInfo

@Dao
interface TimelineInfoDao : IDao<TimelineInfo> {

    @Query("SELECT id,type,account_id,foreign_id FROM timeline_info " +
            "INNER JOIN timeline_group_to_timeline_info " +
            "ON timeline_info.id = timeline_group_to_timeline_info.info_id " +
            "WHERE timeline_group_to_timeline_info.group_name = :groupName " +
            "ORDER BY order_num")
    fun selectByGroupName(groupName: String): List<TimelineInfo>

    @Query("SELECT id,type,account_id,foreign_id FROM timeline_info " +
            "INNER JOIN timeline_group_to_timeline_info " +
            "ON timeline_info.id = timeline_group_to_timeline_info.info_id " +
            "WHERE timeline_group_to_timeline_info.group_name = :groupName AND timeline_group_to_timeline_info.order_num > :order " +
            "ORDER BY order_num")
    fun selectByGroupGreaterThan(groupName: String, order: Int): List<TimelineInfo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun createOrUpdateGroupToInfo(groupToInfo: TlGroupToTlInfo)

    @Query("SELECT COUNT(group_name) FROM timeline_group_to_timeline_info WHERE group_name=:groupName")
    fun countInfoByGroupName(groupName: String): Int

    @Query("SELECT order_num FROM timeline_group_to_timeline_info WHERE group_name = :groupName AND info_id = :infoId")
    fun checkOrder(groupName: String, infoId: Int): Int?

    @Query("SELECT * FROM timeline_info WHERE type = :type AND account_id = :accountId AND foreign_id = :foreignId")
    fun select(type: TlType, accountId: Long, foreignId: Long = -1): TimelineInfo?

    @Query("SELECT * FROM timeline_info")
    fun selectAll(): List<TimelineInfo>

    @Query("DELETE FROM timeline_group_to_timeline_info WHERE group_name = :groupName AND info_id = :infoId")
    fun deleteRelationByGroupName(infoId: Int, groupName: String)

    @Query("SELECT COUNT(info_id) FROM timeline_group_to_timeline_info WHERE info_id = :infoId")
    fun countDependent(infoId: Int): Int
}