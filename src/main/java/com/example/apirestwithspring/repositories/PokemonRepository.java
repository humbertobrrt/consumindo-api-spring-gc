package com.example.apirestwithspring.repositories;

import com.example.apirestwithspring.entities.Pokemon;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


@Repository
public class PokemonRepository {

    private final Map<Integer, Pokemon> store = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger();

    public List<Pokemon> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparing(Pokemon::id))
                .toList();
    }

    public Optional<Pokemon> findById(Integer id) {
        return Optional.ofNullable(store.get(id));
    }

    public Pokemon create(String name, double height) {
        Pokemon pokemon = new Pokemon(sequence.incrementAndGet(), name, height);
        store.put(pokemon.id(), pokemon);
        return pokemon;
    }

    public Optional<Pokemon> update(Integer id, String name, double height) {
        return Optional.ofNullable(
                store.computeIfPresent(id, (key, current) -> new Pokemon(key, name, height)));
    }

    public boolean deleteById(Integer id) {
        return store.remove(id) != null;
    }
}
