package br.pucpr.cursolab.auth

import br.pucpr.cursolab.config.BEARER_AUTH
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/auth")
@Tag(name = "0. Autenticação", description = "Login do operador e identificação por token JWT")
class AuthController(private val service: AuthService) {
    @PostMapping("/login")
    @Operation(summary = "Autentica o operador e emite um JWT")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<LoginResponse> =
        ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.login(request))

    @GetMapping("/me")
    @SecurityRequirement(name = BEARER_AUTH)
    @Operation(summary = "Identifica o operador autenticado")
    fun me(principal: Principal) = OperadorResponse(principal.name)
}
