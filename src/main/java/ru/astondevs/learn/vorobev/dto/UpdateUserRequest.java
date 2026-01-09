package ru.astondevs.learn.vorobev.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Size(min = 2, max = 100, message = "Имя должно содержать от 2 до 100 символов")
    private String name;

    @Email(message = "Некорректный формат email")
    @Size(max = 100, message = "Email должен содержать не более 100 символов")
    private String email;

    @Min(value = 1, message = "Возраст должен быть не менее 1 года")
    @Max(value = 150, message = "Возраст должен быть не более 150 лет")
    private Integer age;
}
