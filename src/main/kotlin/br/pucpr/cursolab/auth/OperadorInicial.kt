package br.pucpr.cursolab.auth

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OperadorInicial(
    private val operadores: OperadorRepository,
    private val encoder: PasswordEncoder,
    @Value("\${app.auth.usuario:operador}") private val usuarioConfigurado: String,
    @Value("\${app.auth.senha:}") private val senhaConfigurada: String
) : ApplicationRunner {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun run(args: ApplicationArguments) {
        if (senhaConfigurada.isEmpty()) {
            if (operadores.count() == 0L)
                log.info("Defina CURSOLAB_OPERADOR_SENHA antes de iniciar para cadastrar a conta de acesso.")
            return
        }

        val usuario = usuarioConfigurado.trim().lowercase()
        require(usuario.matches(Regex("[a-z0-9._-]{1,80}"))) {
            "CURSOLAB_OPERADOR_USUARIO deve ter até 80 letras sem acento, números, pontos, traços ou sublinhados."
        }
        require(senhaConfigurada.isNotBlank() && senhaConfigurada.length >= 8 &&
            senhaConfigurada.toByteArray(Charsets.UTF_8).size <= MAX_BYTES_SENHA) {
            "CURSOLAB_OPERADOR_SENHA deve ter pelo menos 8 caracteres e no máximo 72 bytes."
        }

        val operador = operadores.findByIdOrNull(usuario)
        if (operador != null && encoder.matches(senhaConfigurada, operador.senhaHash)) return

        val hash = checkNotNull(encoder.encode(senhaConfigurada))
        if (operador == null) {
            operadores.save(Operador(usuario, hash))
            log.info("Operador cadastrado: usuario={}", usuario)
        } else {
            operador.senhaHash = hash
            log.info("Senha do operador atualizada: usuario={}", usuario)
        }
    }
}
