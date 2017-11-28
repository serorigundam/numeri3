package tech.ketc.numeri.ui.model

import android.arch.lifecycle.ViewModel
import tech.ketc.numeri.domain.repository.IAccountRepository
import tech.ketc.numeri.domain.repository.IImageRepository
import tech.ketc.numeri.domain.repository.ITwitterUserRepository
import tech.ketc.numeri.ui.model.delegate.ClientHandler
import tech.ketc.numeri.ui.model.delegate.IClientHandler
import tech.ketc.numeri.ui.model.delegate.IImageLoadable
import tech.ketc.numeri.ui.model.delegate.ImageLoadable
import javax.inject.Inject

class TweetViewModel @Inject constructor(accountRepository: IAccountRepository,
                                         userRepository: ITwitterUserRepository,
                                         imageRepository: IImageRepository)
    : ViewModel(),
        IClientHandler by ClientHandler(accountRepository, userRepository),
        IImageLoadable by ImageLoadable(imageRepository)