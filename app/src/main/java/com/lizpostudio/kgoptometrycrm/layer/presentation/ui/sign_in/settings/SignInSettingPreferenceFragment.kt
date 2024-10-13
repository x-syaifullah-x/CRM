package com.lizpostudio.kgoptometrycrm.layer.presentation.ui.sign_in.settings

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.preference.DropDownPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.lizpostudio.kgoptometrycrm.R
import com.lizpostudio.kgoptometrycrm.layer.domain.model.ConfigModel
import com.lizpostudio.kgoptometrycrm.layer.presentation.ui.sign_in.SignInViewModel
import org.json.JSONObject
import kotlin.getValue

class SignInSettingPreferenceFragment : PreferenceFragmentCompat() {

    private val dropDownPreference by lazy {
        preferenceManager.findPreference<DropDownPreference>(getString(R.string.key_use_dropdown))
    }

    private val valueDefault by lazy {
        resources.getStringArray(R.array.use)[0]
    }

    private val viewModel by viewModels<SignInViewModel> { SignInViewModel.Factory }

    private val openDocument =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri == null)
                return@registerForActivityResult
            val openInputStream = context?.contentResolver?.openInputStream(uri)
            if (openInputStream == null)
                return@registerForActivityResult

            val c = requireContext()
            try {
                val googleServiceJSON = JSONObject(String(openInputStream.readBytes()))
                val config = ConfigModel.from(googleServiceJSON)
                if (config == null) {
                    Toast.makeText(c, "Invalid Json File", Toast.LENGTH_LONG).show()
                    return@registerForActivityResult
                }
                val isConfigChange = viewModel.changeFirebaseConfig(c, config)
                val message =
                    if (isConfigChange)
                        "Successful change configuration"
                    else
                        "Invalid change configuration, please try again"
                Toast.makeText(c, message, Toast.LENGTH_LONG).show()
            } catch (t: Throwable) {
                Toast.makeText(c, t.message, Toast.LENGTH_LONG).show()
            } finally {
                openInputStream.close()
            }
        }


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.login_prefs, rootKey)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val selectFile = preferenceManager
            .findPreference<Preference>(getString(R.string.key_select_file_preference))
        val valueSelectFile = resources.getStringArray(R.array.use)[1]

        selectFile?.isEnabled = dropDownPreference?.value == valueSelectFile

        dropDownPreference?.setOnPreferenceChangeListener { preference, newValue ->
            selectFile?.isEnabled = "$newValue" == valueSelectFile
            if ("$newValue" == valueDefault) {
                viewModel.setDefaultFirebaseConfig(preference.context)
            }
            true
        }

        selectFile?.setOnPreferenceClickListener {
            openDocument.launch(arrayOf("application/json"))
            true
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    fun clear(context: Context?) {
//        val sharedPreferences = Constants.getSharedPreferences(context)
//        val editor = sharedPreferences.edit()
//        editor.putLong(Constants.PREF_KEY_LAST_SYNC, 0)
//        editor.putBoolean(Constants.PREF_KEY_FIRE_FETCHED, false)
//        editor.apply()
    }
}