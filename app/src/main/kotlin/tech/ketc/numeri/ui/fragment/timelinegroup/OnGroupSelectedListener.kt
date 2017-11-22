package tech.ketc.numeri.ui.fragment.timelinegroup

import tech.ketc.numeri.infra.entity.TimelineGroup

interface OnGroupSelectedListener {
    fun onGroupSelected(group: TimelineGroup)
}