package ru.citadel.rise.login.registration

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import ru.citadel.rise.R
import ru.citadel.rise.data.model.Resource
import ru.citadel.rise.data.model.Result
import ru.citadel.rise.data.model.User
import ru.citadel.rise.data.remote.RemoteRepository

class RegistrationViewModel : ViewModel() {

    private val remoteRepository = RemoteRepository()

    private val _form = MutableLiveData(RegistrationFormState())
    val registrationFormState: LiveData<RegistrationFormState> = _form


    private val _isPersonChecked = MutableLiveData(true)
    private val _promptName = MutableLiveData("Ваше имя")

    val isPersonChecked: LiveData<Boolean> = _isPersonChecked
    val promptName: LiveData<String> = _promptName

    fun onPersonChecked(){
        _isPersonChecked.value = true
        _promptName.value = "Ваше имя"
    }

    fun onCompanyChecked(){
        _isPersonChecked.value = false
        _promptName.value = "Имя вашей компании"
    }

    fun createNew(name: String, type: Int, email: String, password: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            val result: Result<User> = remoteRepository.registr(email.hashCode(), name, password.hashCode(), type, email)
            if (result.error == null)
                emit(Resource.success(data = result.data))
            else
                emit(Resource.error(data = null, message = result.error))
        } catch (e: Exception){
            emit(Resource.error(data = null, message = e.message.toString()))
        }
    }

    fun loginDataChanged(email: String, name: String, password: String) {
        _form.value =
            RegistrationFormState(emailError = validateEmail(email),
                nameError = validateName(name),
                passwordError = validatePassword(password),
                isDataValid = isAllDataValid(email, name, password))
    }

    private fun isAllDataValid(email: String, name: String, password: String)
        = Patterns.EMAIL_ADDRESS.matcher(email).matches() and name.isNotBlank() and (password.length >= 8)

    private fun validateEmail(email: String) =
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) null else R.string.invalid_email

    private fun validateName(name: String) =
        if (name.isNotBlank()) null else R.string.invalid_username

    private fun validatePassword(password: String) =
        if (password.length >= 8) null else R.string.invalid_password
}