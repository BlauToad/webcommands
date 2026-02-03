package de.blautoad.webcommands_mixins;

import de.blautoad.webcommands.Config;
import de.blautoad.webcommands.Listener;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.net.ServerSocket;

@Mixin(PauseScreen.class)
public abstract class GameMenuScreenMixin {

    // Declare a new text field widget for the number input
    @Unique
    private EditBox numberInput;

    // Declare a new button widget for changing the port
    @Unique
    private Button changePortButton;
    // Declare a new button widget for changing the port
    @Unique
    private Button openWebButton;

    @Mixin(Screen.class)
    public interface ScreenAccessor {
        @Accessor
        Font getFont();
        @Invoker
        <T extends GuiEventListener & Renderable & NarratableEntry> T invokeAddRenderableWidget(GuiEventListener element);
    }

    // Inject some code at the end of the init method of the game menu screen
    @Inject(method = "init", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        // Get the game menu screen instance
        PauseScreen screen = (PauseScreen) (Object) this;
        // Position & Size of Port Settings
        int[] position_x_y = {screen.width / 2 + 105, screen.height / 4 + 81 - 24};
        int[] size_width_height = {100, 18};

        // Initialize the number input widget with some parameters
        numberInput = new EditBox(((ScreenAccessor) screen).getFont(), position_x_y[0], position_x_y[1], size_width_height[0], size_width_height[1], Component.empty());
        numberInput.setMaxLength(5); // Set the maximum length of the input to 5 characters
        numberInput.setValue(String.valueOf(Config.getPort())); // Set the default text of the input to value of config
        numberInput.setResponder(this::onNumberInputChanged); // Set a listener for when the input changes

        // Initialize the change port button widget with some parameters
        Button.Builder bwb = new Button.Builder(Component.empty(), this::onChangePortButtonPressed);
        bwb.pos(position_x_y[0]-1, position_x_y[1] + 23);
        bwb.size(size_width_height[0]+2, size_width_height[1]+2);
        changePortButton = bwb.build();

        // Add open on web button
        Button.Builder bwb2 = new Button.Builder(Component.translatable("webcommands.change_port.open_web_panel"), this::openWebButtonPressed);
        bwb2.pos(position_x_y[0]-1, position_x_y[1] + 23*2 + 1);
        bwb2.size(size_width_height[0]+2, size_width_height[1]+2);
        openWebButton = bwb2.build();

        // Add the widgets to the screen

        ((ScreenAccessor) screen).invokeAddRenderableWidget(numberInput);
        ((ScreenAccessor) screen).invokeAddRenderableWidget(changePortButton);
        ((ScreenAccessor) screen).invokeAddRenderableWidget(openWebButton);

        // Display
        onNumberInputChanged(String.valueOf(Config.getPort()));

    }

    // Define a method for when the number input changes
    @Unique
    private void onNumberInputChanged(String text) {
        // Try to parse the text as an integer
        try {
            int port = Integer.parseInt(text);
            // Check if the port is valid (between 0 and 65535)
            if (port != Config.getPort()) {
                if(port >= 1 && port <= 65535){
                    try (ServerSocket serverSocket = new ServerSocket(port)) {
                        // port is available
                        serverSocket.close();
                        // Set the button to be active and return
                        changePortButton.active = true;
                        changePortButton.setMessage(Component.translatable("webcommands.change_port.change_port"));
                        numberInput.setTextColor(0xFFE0E0E0);
                        return;
                    } catch (IOException e) {
                        // port is already used
                        changePortButton.setMessage(Component.translatable("webcommands.change_port.occupied"));
                    }
                }else{
                    changePortButton.setMessage(Component.translatable("webcommands.change_port.out_of_range"));
                }
            }else{
                if(Config.isTempPort()){
                    changePortButton.active = true;
                    if(isFallbackPort(port)){
                        changePortButton.setMessage(Component.translatable("webcommands.change_port.using_fallbackport"));
                        numberInput.setTextColor(0xFFFFAA00);
                    }else{
                        changePortButton.setMessage(Component.translatable("webcommands.change_port.temp_free_port"));
                        numberInput.setTextColor(0xFFE0E0E0);
                    }
                }else{
                    changePortButton.setMessage(Component.translatable("webcommands.change_port.current_port"));
                    changePortButton.active = false;
                    numberInput.setTextColor(0xFF00A900);
                }
                return;
            }
        } catch (NumberFormatException e) {
            // Ignore the exception
            changePortButton.setMessage(Component.translatable("webcommands.change_port.invalid_input"));
        }
        // If the text is not a valid port, set the button to be inactive
        changePortButton.active = false;
        numberInput.setTextColor(0xFFA90000);
    }

    @Unique
    private boolean isFallbackPort(int port){
        int[] fbp = Config.getFallbackPorts();

        boolean r = false;
        for (int j : fbp) {
            if (j == port) {
                r = true;
                break;
            }
        }
        return r;
    }

    // Define a method for when the change port button is pressed
    @Unique
    private void onChangePortButtonPressed(Button button) {
        // Get the current text of the number input
        String text = numberInput.getValue();
        // Try to parse it as an integer
        try {
            int port = Integer.parseInt(text);
            // Save the port to some file or variable (this is up to you)
            savePort(port);
        } catch (NumberFormatException e) {
            // Ignore the exception
        }
    }

    @Unique
    private void openWebButtonPressed(Button button){
        Util.getPlatform().openUri("http://localhost:" + Config.getPort()+"/builder");
    }

    // Define a method for saving the port (this is up to you)
    @Unique
    private void savePort(int port) {
        Config.savePort(port);
        Listener.restartServerSocket();
        changePortButton.setMessage(Component.translatable("webcommands.change_port.success"));
        changePortButton.active = false;
        numberInput.setTextColor(0x00A900);
    }
}
