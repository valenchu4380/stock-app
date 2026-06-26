package com.valentin.tu_cv_spring_bot.TuCv.service;

import java.util.*;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Linea;
import org.springframework.stereotype.Service;

@Service
public class LineaDetectionService {

    private static final Map<String, Map<String, List<Pattern>>> PATTERNS = new LinkedHashMap<>();

    static {
        Map<String, List<Pattern>> avon = new LinkedHashMap<>();
        avon.put("PERFUME", Arrays.asList(
            new Pattern("TTA|Today Tomorrow Always|Today.+Tomorrow.+Always", Linea.TODAY_TOMORROW_ALWAYS),
            new Pattern("Attraction", Linea.ATTRACTION),
            new Pattern("Far Away", Linea.FAR_AWAY),
            new Pattern("Imari", Linea.IMARI),
            new Pattern("Soft Musk", Linea.SOFT_MUSK),
            new Pattern("Sweet Honesty", Linea.SWEET_HONESTY),
            new Pattern("Pasi[oó]n Gitana", Linea.PASION_GITANA),
            new Pattern("Maxime", Linea.MAXIME),
            new Pattern("Lucky for Me", Linea.LUCKY_FOR_ME),
            new Pattern("Lov[\\|]?U", Linea.LOV_U),
            new Pattern("Mesmerize", Linea.MESMERIZE),
            new Pattern("Princesa", Linea.PRINCESA)
        ));
        avon.put("MAQUILLAJE", Arrays.asList(
            new Pattern("Ultra Color", Linea.ULTRA_COLOR),
            new Pattern("Mark", Linea.MARK),
            new Pattern("Color Trend", Linea.COLOR_TREND),
            new Pattern("Epic Lip", Linea.EPIC_LIP),
            new Pattern("Unlimited", Linea.UNLIMITED),
            new Pattern("Avon Color", Linea.AVON_COLOR),
            new Pattern("True", Linea.TRUE_LINE)
        ));
        avon.put("CABELLO", Arrays.asList(
            new Pattern("Lotus Shield", Linea.LOTUS_SHIELD),
            new Pattern("Spiderman|Avengers|Frozen|Buzz|Disney|Princesa", Linea.DISNEY),
            new Pattern("Avon Naturals", Linea.AVON_NATURALS)
        ));
        avon.put("CREMA", Arrays.asList(
            new Pattern("Anew", Linea.ANEW),
            new Pattern("Avon Care", Linea.AVON_CARE),
            new Pattern("Simply Delicate", Linea.SIMPLY_DELICATE)
        ));
        avon.put("CUIDADO_DIARIO", Arrays.asList(
            new Pattern("Foot Works|FootWorks", Linea.FOOT_WORKS),
            new Pattern("Encanto", Linea.ENCANTO),
            new Pattern("On Duty", Linea.ON_DUTY),
            new Pattern("Casa.*Estilo|Casa & Estilo", Linea.CASA_ESTILO),
            new Pattern("Care", Linea.CARE_LINE)
        ));

        Map<String, List<Pattern>> natura = new LinkedHashMap<>();
        natura.put("PERFUME", Arrays.asList(
            new Pattern("Kaiak", Linea.KAIAK),
            new Pattern("Humor", Linea.HUMOR),
            new Pattern("Kriska", Linea.KRISKA),
            new Pattern("Essencial", Linea.ESSENCIAL),
            new Pattern("Natura Homem|Homem", Linea.NATURA_HOMEM),
            new Pattern("Luna", Linea.LUNA),
            new Pattern("Sr\\.? N|Sr N", Linea.SR_N),
            new Pattern("Sinton[íi]a", Linea.SINTONIA),
            new Pattern("Il[íi]a", Linea.ILIA),
            new Pattern("Biograf[íi]a", Linea.BIOGRAFIA),
            new Pattern("[áA]guas|Aguas", Linea.AGUAS)
        ));
        natura.put("MAQUILLAJE", Arrays.asList(
            new Pattern("Una", Linea.UNA),
            new Pattern("Faces", Linea.FACES),
            new Pattern("Colecci[oó]n Libre|Coleccion Libre", Linea.COLECCION_LIBRE)
        ));
        natura.put("CABELLO", Arrays.asList(
            new Pattern("Lumina", Linea.LUMINA),
            new Pattern("Ekos", Linea.EKOS_CABELLO)
        ));
        natura.put("CREMA", Arrays.asList(
            new Pattern("Chronos", Linea.CHRONOS),
            new Pattern("Ekos", Linea.EKOS_CREMA),
            new Pattern("Tododia", Linea.TODODIA_CREMA)
        ));
        natura.put("CUIDADO_DIARIO", Arrays.asList(
            new Pattern("Tododia", Linea.TODODIA_DIARIO),
            new Pattern("Ekos", Linea.EKOS_DIARIO),
            new Pattern("Frescor", Linea.FRESCOR)
        ));

        PATTERNS.put("AVON", avon);
        PATTERNS.put("NATURA", natura);
    }

    public Linea detectarLinea(String productName, String category, String subCategory) {
        if (productName == null || productName.isBlank()) return null;
        String upper = productName.toUpperCase();

        Map<String, List<Pattern>> catPatterns = PATTERNS.get(category);
        if (catPatterns == null) return null;

        List<Pattern> subPatterns = catPatterns.get(subCategory);
        if (subPatterns == null) {
            subPatterns = new ArrayList<>();
            for (List<Pattern> ps : catPatterns.values()) subPatterns.addAll(ps);
        }

        for (Pattern p : subPatterns) {
            if (upper.matches(".*" + p.regex.toUpperCase() + ".*")) {
                return p.linea;
            }
        }
        return null;
    }

    public List<Linea> getLineasPorCategoriaYSub(String category, String subCategory) {
        Map<String, List<Pattern>> catPatterns = PATTERNS.get(category);
        if (catPatterns == null) return Collections.emptyList();
        List<Pattern> subPatterns = catPatterns.get(subCategory);
        if (subPatterns == null) return Collections.emptyList();
        return subPatterns.stream().map(p -> p.linea).distinct().toList();
    }

    private record Pattern(String regex, Linea linea) {}
}
