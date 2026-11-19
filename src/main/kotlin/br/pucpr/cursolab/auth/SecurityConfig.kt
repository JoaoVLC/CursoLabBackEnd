package br.pucpr.cursolab.auth

import jakarta.servlet.DispatcherType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtClaimNames
import org.springframework.security.oauth2.jwt.JwtClaimValidator
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtIssuerValidator
import org.springframework.security.oauth2.jwt.JwtTimestampValidator
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import java.time.Duration
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

@Configuration
class SecurityConfig {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun userDetailsService(operadores: OperadorRepository): UserDetailsService = UserDetailsService { usuario ->
        val operador = operadores.findByIdOrNull(usuario)
            ?: throw UsernameNotFoundException("Operador não encontrado.")
        User.withUsername(operador.usuario).password(operador.senhaHash).roles("OPERADOR").build()
    }

    @Bean
    fun authenticationManager(usuarios: UserDetailsService, encoder: PasswordEncoder): AuthenticationManager {
        val provider = DaoAuthenticationProvider(usuarios)
        provider.setPasswordEncoder(encoder)
        return ProviderManager(provider)
    }

    // A chave de assinatura é gerada a cada início e permanece na memória do servidor.
    @Bean
    fun chaveJwt(): SecretKey = KeyGenerator.getInstance("HmacSHA256").apply { init(256) }.generateKey()

    @Bean
    fun jwtEncoder(chaveJwt: SecretKey): JwtEncoder =
        NimbusJwtEncoder.withSecretKey(chaveJwt).algorithm(MacAlgorithm.HS256).build()

    @Bean
    fun jwtDecoder(chaveJwt: SecretKey): JwtDecoder {
        val decoder = NimbusJwtDecoder.withSecretKey(chaveJwt).macAlgorithm(MacAlgorithm.HS256).build()
        val tempo = JwtTimestampValidator(Duration.ZERO).apply { setAllowEmptyExpiryClaim(false) }
        val titular = JwtClaimValidator<String>(JwtClaimNames.SUB) { !it.isNullOrBlank() }
        decoder.setJwtValidator(DelegatingOAuth2TokenValidator(tempo, JwtIssuerValidator(EMISSOR_JWT), titular))
        return decoder
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain = http
        // A API autentica pelo cabeçalho Authorization e funciona sem sessão ou cookies de login.
        .csrf { it.disable() }
        .httpBasic { it.disable() }
        .formLogin { it.disable() }
        .logout { it.disable() }
        .requestCache { it.disable() }
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .exceptionHandling { it.authenticationEntryPoint(BearerTokenAuthenticationEntryPoint()) }
        .authorizeHttpRequests { regras ->
            regras.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                .requestMatchers(
                    HttpMethod.GET,
                    "/", "/health", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**",
                    "/v3/api-docs.yaml", "/categorias", "/cursos", "/cursos/{id}"
                ).permitAll()
                .anyRequest().hasAuthority("SCOPE_$ESCOPO_OPERADOR")
        }
        .oauth2ResourceServer { it.jwt(Customizer.withDefaults()) }
        .build()
}
