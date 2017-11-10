package tech.ketc.numeri.domain.di

import dagger.Subcomponent

@Subcomponent
interface RepositoryComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): RepositoryComponent
    }
}