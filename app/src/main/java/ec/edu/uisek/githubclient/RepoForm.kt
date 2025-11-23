package ec.edu.uisek.githubclient

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ec.edu.uisek.githubclient.databinding.ActivityRepoFormBinding
import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.models.RepoRequest
import ec.edu.uisek.githubclient.services.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RepoForm : AppCompatActivity() {
    private lateinit var repoformbinding: ActivityRepoFormBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repoformbinding = ActivityRepoFormBinding.inflate(layoutInflater)
        setContentView(repoformbinding.root)
        repoformbinding.buttonCancelRepo.setOnClickListener{finish()}
        repoformbinding.buttonSaveRepo.setOnClickListener{createRepoForm()}
    }

    private fun validateInput(): Boolean {
        val reponame = repoformbinding.nameRepoInputForm.text.toString()
        if (

            reponame.isBlank()
        ) {
            repoformbinding.nameRepoInputForm.error = "El nombre del repositorio es obligatorio"
            return false
        }
        if (
            reponame.contains(" ")
        ) {
            repoformbinding.nameRepoInputForm.error =
                "El nombre del repositorio no puede contener espacios"
            return false
        }
        return true
    }

    private fun createRepoForm() {
        if (!validateInput()) {
            return
        }
        val reponame = repoformbinding.nameRepoInputForm.text.toString()
        val repodescription = repoformbinding.descriptionRepoInputForm.text.toString()
        val repoRequest: RepoRequest = RepoRequest(
            name = reponame,
            description = repodescription
        )
        val apiServices = RetrofitClient.gitHubApiService
        val call = apiServices.createRepo(repoRequest)
        call.enqueue(object : Callback<Repo>{
            override fun onResponse(call: Call<Repo?>, response: Response<Repo?>) {
                if(response.isSuccessful){
                    Log.d("RepoForm", "El repositorio ${reponame} fue creado exitosamente")
                    showMessage("El repositorio ${reponame} fue creado exitosamente")
                    finish()
                }else {
                    //no hay respuesta
                    val errorMessage = when(response.code()){
                        401 -> "Error de autenticacion"
                        403 -> "Recurso no permitido"
                        404 -> "Recurso no encontrado"
                        else -> "Error desconociido ${response.code()}: ${response.message()}"
                    }
                    //voy a lanzar un error
                    Log.e("RepoForm", errorMessage)
                    //muestro el mensaje
                    showMessage(errorMessage)
                }

            }

            override fun onFailure(call: Call<Repo?>, t: Throwable) {
                Log.e("RepoForm", "Error de red: ${t.message}")
                showMessage("Error de red: ${t.message}")
            }
        })
    }
    private fun showMessage(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

}