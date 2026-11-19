package br.pucpr.cursolab.auth

import org.springframework.data.jpa.repository.JpaRepository

interface OperadorRepository : JpaRepository<Operador, String>
