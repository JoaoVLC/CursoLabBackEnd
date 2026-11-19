package br.pucpr.cursolab.auth

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

internal const val MAX_BYTES_SENHA = 72

class LoginRequest(
    @field:NotBlank(message = "Informe o usuário.")
    @field:Size(max = 80)
    val usuario: String,

    @field:NotBlank(message = "Informe a senha.")
    @field:Size(max = MAX_BYTES_SENHA)
    val senha: String
)

data class LoginResponse(val accessToken: String, val tokenType: String, val expiresIn: Long)

data class OperadorResponse(val usuario: String)
