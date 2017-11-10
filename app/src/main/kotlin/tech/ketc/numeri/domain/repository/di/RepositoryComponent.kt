package tech.ketc.numeri.domain.repository.di

import dagger.Subcomponent

@Subcomponent
interface RepositoryComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): RepositoryComponent
    }
}