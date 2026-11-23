package com.enofex.taikai.exports.entrypoints.web;

import com.enofex.taikai.exports.entrypoints.api.CrudApi;
import org.springframework.web.bind.annotation.RestController;

@RestController
class NoteController extends NoteStore implements CrudApi<String> {

}
