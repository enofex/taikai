package com.enofex.taikai.demo.violations

import org.springframework.stereotype.Service
import org.springframework.context.annotation.Profile

data class MutableResponse(var title: String)
@Service
@Profile("architecture-violations")
class WronglyNamed
