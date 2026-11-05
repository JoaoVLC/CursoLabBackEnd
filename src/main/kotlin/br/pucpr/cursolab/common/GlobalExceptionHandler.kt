package br.pucpr.cursolab.common

import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.*
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, "Dados inválidos.")
        problem.setProperty("errors", ex.bindingResult.fieldErrors
            .groupBy({ it.field }, { it.defaultMessage ?: "Valor inválido" }))
        return createResponseEntity(problem, headers, HttpStatus.UNPROCESSABLE_CONTENT, request)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun constraint(ex: ConstraintViolationException): ResponseEntity<ProblemDetail> {
        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, "Parâmetros inválidos.")
        problem.setProperty("errors", ex.constraintViolations
            .groupBy({ it.propertyPath.toString() }, { it.message }))
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(problem)
    }

    @ExceptionHandler(BusinessException::class)
    fun business(ex: BusinessException): ResponseEntity<ProblemDetail> {
        log.warn("Regra de negócio: status={}, mensagem={}", ex.status.value(), ex.message)
        return ResponseEntity.status(ex.status)
            .body(ProblemDetail.forStatusAndDetail(ex.status, ex.message ?: "Operação inválida."))
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun integrity(ex: DataIntegrityViolationException): ResponseEntity<ProblemDetail> {
        log.warn("Operação bloqueada por restrição de integridade do banco.")
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Registro duplicado ou com vínculos que impedem esta operação."))
    }

    @ExceptionHandler(Exception::class)
    fun unexpected(ex: Exception): ResponseEntity<ProblemDetail> {
        log.error("Erro inesperado no servidor.", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor."))
    }
}
