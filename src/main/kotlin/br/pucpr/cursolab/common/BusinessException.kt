package br.pucpr.cursolab.common

import org.springframework.http.HttpStatus

open class BusinessException(val status: HttpStatus, message: String) : RuntimeException(message)
class NotFoundException(message: String) : BusinessException(HttpStatus.NOT_FOUND, message)
class ConflictException(message: String) : BusinessException(HttpStatus.CONFLICT, message)
class BadRequestException(message: String) : BusinessException(HttpStatus.BAD_REQUEST, message)
