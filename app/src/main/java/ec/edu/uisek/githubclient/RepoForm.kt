package ec.edu.uisek.githubclient

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ec.edu.uisek.githubclient.databinding.ActivityRepoFormBinding
import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.models.RepoRequest
import ec.edu.uisek.githubclient.services.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RepoForm : AppCompatActivity() {

    private lateinit var repoformbinding: ActivityRepoFormBinding
    private var mode: String = "CREATE" //El modo de crear repo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repoformbinding = ActivityRepoFormBinding.inflate(layoutInflater)
        setContentView(repoformbinding.root)

        // aqui lo que hacemos es detectar el modo desde el Intent
        mode = intent.getStringExtra("MODE") ?: "CREATE"

        if (mode == "EDIT") {
            val repoName = intent.getStringExtra("REPO_NAME") ?: ""
            val repoDesc = intent.getStringExtra("REPO_DESC") ?: ""
            val repoLang = intent.getStringExtra("REPO_LANG") ?: ""

            repoformbinding.nameRepoInputForm.setText(repoName)
            repoformbinding.nameRepoInputForm.isEnabled = false // aqui no va a permitir cambiar nombre del repo
            repoformbinding.descriptionRepoInputForm.setText(repoDesc)
            repoformbinding.buttonSaveRepo.text = "Actualizar"

            repoformbinding.buttonSaveRepo.setOnClickListener { updateRepo() }

        } else {
            repoformbinding.buttonSaveRepo.setOnClickListener { createRepoForm() }
        }

        repoformbinding.buttonCancelRepo.setOnClickListener { finish() }
    }

    private fun validateInput(): Boolean {
        val reponame = repoformbinding.nameRepoInputForm.text.toString()
        if (reponame.isBlank()) {
            repoformbinding.nameRepoInputForm.error = "El nombre del repositorio es obligatorio"
            return false
        }
        if (reponame.contains(" ")) {
            repoformbinding.nameRepoInputForm.error =
                "El nombre del repositorio no puede contener espacios"
            return false
        }
        return true
    }

    private fun createRepoForm() {
        if (!validateInput()) return

        val reponame = repoformbinding.nameRepoInputForm.text.toString()
        val repodescription = repoformbinding.descriptionRepoInputForm.text.toString()
        val repoRequest = RepoRequest(name = reponame, description = repodescription)

        val apiServices = RetrofitClient.gitHubApiService
        val call = apiServices.createRepo(repoRequest)

        call.enqueue(object : Callback<Repo> {
            override fun onResponse(call: Call<Repo>, response: Response<Repo>) {
                if (response.isSuccessful) {
                    Log.d("RepoForm", "El repositorio $reponame fue creado exitosamente")
                    showMessage("El repositorio $reponame fue creado exitosamente")
                    finish()
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Error de autenticacion"
                        403 -> "Recurso no permitido"
                        404 -> "Recurso no encontrado"
                        else -> "Error desconocido ${response.code()}: ${response.message()}"
                    }
                    Log.e("RepoForm", errorMessage)
                    showMessage(errorMessage)
                }
            }

            override fun onFailure(call: Call<Repo>, t: Throwable) {
                Log.e("RepoForm", "Error de red: ${t.message}")
                showMessage("Error de red: ${t.message}")
            }
        })
    }

    // vamos a actualizarl la informacion del repo
    private fun updateRepo() {
        val updatedDesc = repoformbinding.descriptionRepoInputForm.text.toString()
        val repoName = repoformbinding.nameRepoInputForm.text.toString()

        val resultIntent = Intent().apply {
            putExtra("owner", repoName)
            putExtra("repo", updatedDesc)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}

