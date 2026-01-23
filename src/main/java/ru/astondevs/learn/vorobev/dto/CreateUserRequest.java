package ru.astondevs.learn.vorobev.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на создание пользователя")
public class CreateUserRequest {

    @Schema(description = "Имя пользователя", example = "Алексей")
    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 2, max = 100, message = "Имя должно содержать от 2 до 100 символов")
    private String name;

    @Schema(description = "Email пользователя", example = "alex@mail.ru")
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    @Size(max = 100, message = "Email должен содержать не более 100 символов")
    private String email;

    @Schema(description = "Возраст пользователя", example = "20")
    @NotNull(message = "Возраст не может быть пустым")
    @Min(value = 1, message = "Возраст должен быть не менее 1 года")
    @Max(value = 150, message = "Возраст должен быть не более 150 лет")
    private Integer age;
}
