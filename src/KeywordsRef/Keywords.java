package KeywordsRef;

import java.util.Arrays;
import java.util.List;

public record Keywords(List<String> BasicDatatypes, List <String> SpecialDatatypes , List<String> ComplexDatatypes, List<String> BodyKeywords, List<String> SpecialFunctions, List<String> ComparisonKeywords, List<String> OperationalKeywords, List<String> GameSpecKeywords) {
    public Keywords() {
        this(Arrays.asList("Number", "Decimal", "Word"),Arrays.asList("Boolean","void") , Arrays.asList("List", "AnimationFrame"), Arrays.asList("if", "else", "on", "when", "character", "characterType", "forEach", "while"), Arrays.asList("showImage", "getSingularInput", "getRandomNumber", "wait"), Arrays.asList("isEqualTo", "isGreaterThan" ,"isLessThan"), List.of("run"), Arrays.asList("gameStart", "gameLoop", "gameEnd"));
    }
}
