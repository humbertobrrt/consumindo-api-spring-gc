package com.example.apirestwithspring.services;

import com.example.apirestwithspring.entities.Pokemon;
import com.example.apirestwithspring.repositories.PokemonRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class PokemonService {

    private final RestClient client;
    private final PokemonRepository repository;

    public PokemonService(RestClient.Builder builder, PokemonRepository repository) {
        this.client = RestClient.builder().baseUrl("https://pokeapi.co/api/v2").build();
        this.repository = repository;
    }

    public String getPokemonByName (String pokemon_name){
       return  Objects.requireNonNull(client.get()
                       .uri("/pokemon/{pokemon_name}", pokemon_name)
                       .retrieve()
                       .body(Pokemon.class))
                       .toString();
    }

    public List<Pokemon> findAll() {
        return repository.findAll();
    }

    public Optional<Pokemon> findById(Integer id) {
        return repository.findById(id);
    }

    public Pokemon create(Pokemon pokemon) {
        validate(pokemon);
        // o id sempre é gerado pela API; qualquer id enviado no corpo é ignorado
        return repository.create(pokemon.name().trim(), pokemon.height());
    }

    public Optional<Pokemon> update(Integer id, Pokemon pokemon) {
        validate(pokemon);
        return repository.update(id, pokemon.name().trim(), pokemon.height());
    }

    public boolean delete(Integer id) {
        return repository.deleteById(id);
    }

    private void validate(Pokemon pokemon) {
        if (pokemon.name() == null || pokemon.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name must not be blank");
        }
        if (pokemon.height() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "height must be greater than 0");
        }
    }
}
