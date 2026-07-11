package bank;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BankController.class)
class BankControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BankService bankService;

    @Test
    void menynRenderas() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    void personerSidanRenderasMedKontoinnehavare() throws Exception {
        // Regressionstest: sidan gav 500 när listan inte var tom
        // (Thymeleaf 3.1 tillåter inte stränguttryck i th:on*-attribut)
        when(bankService.getAllPersonNames()).thenReturn(List.of("Anna Svensson"));
        mockMvc.perform(get("/registervard/personer"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Anna Svensson")));
    }

    @Test
    void personerSidanRenderasMedTomLista() throws Exception {
        when(bankService.getAllPersonNames()).thenReturn(List.of());
        mockMvc.perform(get("/registervard/personer"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Inga kontoinnehavare")));
    }

    @Test
    void kontoversiktenRenderas() throws Exception {
        when(bankService.getAllAccounts()).thenReturn(List.of(
                new KontoInfo("12345", "spar", "Anna Svensson", 1000.0)));
        mockMvc.perform(get("/kontoversikt"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("12345")));
    }
}
