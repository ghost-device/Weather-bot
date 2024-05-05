package org.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class City {
    Location location;
    Current current;

    @Override
    public String toString() {
        return location + "" + current;
    }

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private class Location {
        String name;
        String country;

        @Override
        public String toString() {
            return  "City: " + name + "\nCountry: " + country;
        }
    }

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private class Current {
        @JsonProperty("temp_c")
        Float tempC;

        @Override
        public String toString() {
            return "\nTemp: " + tempC;
        }
    }
}

