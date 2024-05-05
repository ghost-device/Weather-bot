package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class Bot extends TelegramLongPollingBot {
    ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()){
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            Long id = update.getMessage().getFrom().getId();

            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);

            if (text.equals("/start")) {
                start(id, sendMessage);
            } else {
                String city = update.getMessage().getText();
                getWeather(city, update.getMessage().getChatId());
            }
        } else if (update.hasCallbackQuery()) {
            getWeather(update);
        }
    }

    private void getWeather(Update update) {
        SendMessage sendMessage = new SendMessage();
        CallbackQuery callbackQuery = update.getCallbackQuery();

        List<User> users = getUsers();
        users.add(new User(callbackQuery.getFrom().getId(), callbackQuery.getFrom().getUserName(), callbackQuery.getFrom().getFirstName()));
        updater(users);

        sendMessage.setChatId(callbackQuery.getMessage().getChatId());
        if (callbackQuery.getData().equals("register_clicked")) {
            sendMessage.setText("Enter city");
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void start(Long id, SendMessage sendMessage) {
        if (checkUser(id)){
            sendMessage.setText("Enter city");

            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        } else {
            InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkup();
            sendMessage.setText("Click 'Register' button for register");
            sendMessage.setReplyMarkup(inlineKeyboardMarkup);

            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void getWeather(String text, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        String apiKey = "fb479250afb0459bad9132740240505";
        String apiUrl = "http://api.weatherapi.com/v1/current.json?key=" + apiKey + "&q=" + text;

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(apiUrl)).build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200){
                City city = mapper.readValue(response.body(), new TypeReference<City>() {});
                sendMessage.setText(city.toString());
            } else {
                sendMessage.setText("Not found");
            }

            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static InlineKeyboardMarkup getInlineKeyboardMarkup() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("Register");
        inlineKeyboardButton.setCallbackData("register_clicked");
        rowInLine.add(inlineKeyboardButton);
        rowsLine.add(rowInLine);
        inlineKeyboardMarkup.setKeyboard(rowsLine);
        return inlineKeyboardMarkup;
    }

    private boolean checkUser(Long id) {
        List<User> list = getUsers();

        for (User user : list) {
            if (user.getId().equals(id)) {
                return true;
            }
        }

        return false;
    }

    public void updater(List<User> list){
        try {
            mapper.writeValue(new File("src/main/resources/users.json"), list);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<User> getUsers(){
        try {
            return mapper.readValue(new File("src/main/resources/users.json"), new TypeReference<List<User>>() {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotUsername() {
        return "daily_weather_bot";
    }

    @Override
    public String getBotToken() {
        return "7014866242:AAHIMfUATZqGukKbyBP4ZB-tgV8NVLZoaqk";
    }
}
