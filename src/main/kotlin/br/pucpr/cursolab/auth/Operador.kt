package br.pucpr.cursolab.auth

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "operadores")
class Operador(
    @Id
    @Column(length = 80, updatable = false)
    val usuario: String,

    @Column(length = 60, nullable = false)
    var senhaHash: String
)
