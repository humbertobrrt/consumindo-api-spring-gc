package com.example.apirestwithspring.controllers;

import com.example.apirestwithspring.repositories.PokemonRepository;
import com.example.apirestwithspring.services.PokemonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClient;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testa o CRUD local de Pokémon (/pokeApi/pokemons) sem subir o Spring Boot
 * e sem acessar a rede. O GET que consulta a PokeAPI oficial não é coberto aqui.
 */
class PokemonControllerTests {

    private static final String BASE = "/pokeApi/pokemons";

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        PokemonService service = new PokemonService(RestClient.builder(), new PokemonRepository());
        mvc = MockMvcBuilders.standaloneSetup(new PokemonController(service)).build();
    }

    private void createPokemon(String json) throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
    }

    // ---------- GET ----------

    @Test
    void getListStartsEmpty() throws Exception {
        mvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getListReturnsPokemonsOrderedById() throws Exception {
        createPokemon("{\"name\":\"pikachu\",\"height\":4}");
        createPokemon("{\"name\":\"bulbasaur\",\"height\":7}");

        mvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("pikachu"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("bulbasaur"));
    }

    @Test
    void getByIdReturnsPokemon() throws Exception {
        createPokemon("{\"name\":\"pikachu\",\"height\":4}");

        mvc.perform(get(BASE + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("pikachu"))
                .andExpect(jsonPath("$.height").value(4.0));
    }

    @Test
    void getByUnknownIdReturns404() throws Exception {
        mvc.perform(get(BASE + "/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByNonNumericIdReturns400() throws Exception {
        mvc.perform(get(BASE + "/abc"))
                .andExpect(status().isBadRequest());
    }

    // ---------- POST ----------

    @Test
    void postCreatesPokemonWithGeneratedIdAndLocation() throws Exception {
        mvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  charmander \",\"height\":6}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/pokeApi/pokemons/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("charmander"))
                .andExpect(jsonPath("$.height").value(6.0));
    }

    @Test
    void postIgnoresIdSentByTheClient() throws Exception {
        mvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":500,\"name\":\"squirtle\",\"height\":5}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void postWithBlankNameReturns400() throws Exception {
        mvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"   \",\"height\":5}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postWithoutNameReturns400() throws Exception {
        mvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"height\":5}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postWithMissingOrNonPositiveHeightReturns400() throws Exception {
        mvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"pikachu\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"pikachu\",\"height\":-1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postWithoutBodyReturns400() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ---------- PUT ----------

    @Test
    void putReplacesExistingPokemon() throws Exception {
        createPokemon("{\"name\":\"pikachu\",\"height\":4}");

        mvc.perform(put(BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":999,\"name\":\"raichu\",\"height\":8}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("raichu"))
                .andExpect(jsonPath("$.height").value(8.0));

        mvc.perform(get(BASE + "/1"))
                .andExpect(jsonPath("$.name").value("raichu"));
        mvc.perform(get(BASE))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void putOnUnknownIdReturns404AndDoesNotCreate() throws Exception {
        mvc.perform(put(BASE + "/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"mew\",\"height\":4}"))
                .andExpect(status().isNotFound());

        mvc.perform(get(BASE))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void putWithInvalidBodyReturns400AndKeepsOriginal() throws Exception {
        createPokemon("{\"name\":\"pikachu\",\"height\":4}");

        mvc.perform(put(BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"height\":8}"))
                .andExpect(status().isBadRequest());

        mvc.perform(get(BASE + "/1"))
                .andExpect(jsonPath("$.name").value("pikachu"));
    }

    // ---------- DELETE ----------

    @Test
    void deleteRemovesPokemon() throws Exception {
        createPokemon("{\"name\":\"pikachu\",\"height\":4}");

        mvc.perform(delete(BASE + "/1"))
                .andExpect(status().isNoContent());

        mvc.perform(get(BASE + "/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteOnUnknownIdReturns404() throws Exception {
        mvc.perform(delete(BASE + "/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void idsAreNotReusedAfterDelete() throws Exception {
        createPokemon("{\"name\":\"pikachu\",\"height\":4}");
        mvc.perform(delete(BASE + "/1")).andExpect(status().isNoContent());

        mvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"eevee\",\"height\":3}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2));
    }
}
