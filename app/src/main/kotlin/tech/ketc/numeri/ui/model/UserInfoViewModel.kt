package tech.ketc.numeri.ui.model

import android.arch.lifecycle.ViewModel
import tech.ketc.numeri.domain.repository.IImageRepository
import tech.ketc.numeri.domain.repository.ITwitterUserRepository
import tech.ketc.numeri.ui.model.delegate.IImageLoadable
import tech.ketc.numeri.ui.model.delegate.IUserHandler
import tech.ketc.numeri.ui.model.delegate.ImageLoadable
import tech.ketc.numeri.ui.model.delegate.UserHandler
import javax.inject.Inject

class UserInfoViewModel @Inject constructor(twitterUserRepository: ITwitterUserRepository,
                                            imageRepository: IImageRepository)
    : ViewModel(),
        IUserHandler by UserHandler(twitterUserRepository),
        IImageLoadable by ImageLoadable(imageRepository)