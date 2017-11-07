package tech.ketc.numeri.ui.model

import android.arch.lifecycle.ViewModel
import tech.ketc.numeri.domain.IAccountRepository
import tech.ketc.numeri.util.arch.livedata.AsyncLiveData
import javax.inject.Inject

class MainViewModel @Inject constructor(private val accountRepository: IAccountRepository) : ViewModel() {
    val text = AsyncLiveData { accountRepository.getText() }
}