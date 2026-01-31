package ru.astondevs.learn.vorobev.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на обновление данных существующего пользователя")
public class UpdateUserRequest {

    @Schema(description = "Новое имя пользователя (необязательно)", example = "Иван")
    @Size(min = 2, max = 100, message = "Имя должно содержать от 2 до 100 символов")
    private String name;

    @Schema(description = "Новый email (необязательно)", example = "ivan_new@mail.com")
    @Email(message = "Некорректный формат email")
    @Size(max = 100, message = "Email должен содержать не более 100 символов")
    private String email;

    @Schema(description = "Новый возраст (необязательно)", example = "30")
    @Min(value = 1, message = "Возраст должен быть не менее 1 года")
    @Max(value = 150, message = "Возраст должен быть не более 150 лет")
    private Integer age;
}
