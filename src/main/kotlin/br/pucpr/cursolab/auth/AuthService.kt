package br.pucpr.cursolab.auth

import br.pucpr.cursolab.common.UnauthorizedException
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

internal const val EMISSOR_JWT = "cursolab"
internal const val ESCOPO_OPERADOR = "operador"

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val jwtEncoder: JwtEncoder
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val validade = Duration.ofMinutes(30)

    fun login(request: LoginRequest): LoginResponse {
        val usuario = request.usuario.trim().lowercase()
        val autenticacao = try {
            if (request.senha.toByteArray(Charsets.UTF_8).size > MAX_BYTES_SENHA)
                throw BadCredentialsException("Senha excede o limite do BCrypt.")
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(usuario, request.senha)
            )
        } catch (_: BadCredentialsException) {
            log.warn("Login recusado por credenciais inválidas.")
            throw UnauthorizedException("Usuário ou senha inválidos.")
        }

        val agora = Instant.now()
        val claims = JwtClaimsSet.builder()
            .issuer(EMISSOR_JWT)
            .subject(autenticacao.name)
            .issuedAt(agora)
            .expiresAt(agora.plus(validade))
            .claim("scope", ESCOPO_OPERADOR)
            .build()
        val header = JwsHeader.with(MacAlgorithm.HS256).build()
        val token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims))
        log.info("Operador autenticado: usuario={}", autenticacao.name)
        return LoginResponse(token.tokenValue, "Bearer", validade.seconds)
    }
}
