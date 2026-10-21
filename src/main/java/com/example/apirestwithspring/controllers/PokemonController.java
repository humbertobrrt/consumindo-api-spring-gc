package com.example.apirestwithspring.controllers;

import com.example.apirestwithspring.entities.Pokemon;
import com.example.apirestwithspring.services.PokemonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/pokeApi")
public class PokemonController {

    private final PokemonService service;

    public PokemonController(PokemonService service) {
        this.service = service;
    }

    @GetMapping("/pokemon/{pokemon_name}")
    public String whoIsThatPokemon (@PathVariable String pokemon_name){
        return service.getPokemonByName(pokemon_name);
    }

    @GetMapping("/pokemons")
    public List<Pokemon> listPokemons() {
        return service.findAll();
    }

    @GetMapping("/pokemons/{id}")
    public ResponseEntity<Pokemon> getPokemonById(@PathVariable Integer id) {
        return ResponseEntity.of(service.findById(id));
    }

    @PostMapping("/pokemons")
    public ResponseEntity<Pokemon> createPokemon(@RequestBody Pokemon pokemon) {
        Pokemon created = service.create(pokemon);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/pokemons/{id}")
    public ResponseEntity<Pokemon> updatePokemon(@PathVariable Integer id, @RequestBody Pokemon pokemon) {
        return ResponseEntity.of(service.update(id, pokemon));
    }

    @DeleteMapping("/pokemons/{id}")
    public ResponseEntity<Void> deletePokemon(@PathVariable Integer id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
