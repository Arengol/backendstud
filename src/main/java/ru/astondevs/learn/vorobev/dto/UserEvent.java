package ru.astondevs.learn.vorobev.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEvent {
    private String email;
    private String operation; // "CREATE" или "DELETE"
}