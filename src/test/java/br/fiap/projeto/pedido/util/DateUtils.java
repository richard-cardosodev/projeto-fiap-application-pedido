package br.fiap.projeto.pedido.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateUtils {

    public static Date getCurrentDate(){
        // Obter a data e hora atual
        LocalDateTime currentDateTime = LocalDateTime.now();
        // Converter LocalDateTime para Instant
        Instant instant = currentDateTime.atZone(ZoneId.systemDefault()).toInstant();
        // Criar um objeto Date a partir do Instant
        return Date.from(instant);
    }
}
