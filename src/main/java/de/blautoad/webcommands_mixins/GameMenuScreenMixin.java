package de.blautoad.webcommands_mixins;

import de.blautoad.webcommands.Config;
import de.blautoad.webcommands.Listener;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
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

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin {

    // Declare a new text field widget for the number input
    @Unique
    private TextFieldWidget numberInput;

    // Declare a new button widget for changing the port
    @Unique
    private ButtonWidget changePortButton;
    // Declare a new button widget for changing the port
    @Unique
    private ButtonWidget openWebButton;

    @Mixin(Screen.class)
    public interface ScreenAccessor {
        @Accessor
        TextRenderer getTextRenderer();
        @Invoker
        <T extends Element & Drawable & Selectable> T invokeAddDrawableChild(Element element);
    }

    // Inject some code at the end of the init method of the game menu screen
    @Inject(method = "init", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        // Get the game menu screen instance
        GameMenuScreen screen = (GameMenuScreen) (Object) this;
        // Position & Size of Port Settings
        int[] position_x_y = {screen.width / 2 + 105, screen.height / 4 + 81 - 24};
        int[] size_width_height = {100, 18};

        // Initialize the number input widget with some parameters
        numberInput = new TextFieldWidget(((ScreenAccessor) screen).getTextRenderer(), position_x_y[0], position_x_y[1], size_width_height[0], size_width_height[1], Text.empty());
        numberInput.setMaxLength(5); // Set the maximum length of the input to 5 characters
        numberInput.setText(String.valueOf(Config.getPort())); // Set the default text of the input to value of config
        numberInput.setChangedListener(this::onNumberInputChanged); // Set a listener for when the input changes

        // Initialize the change port button widget with some parameters
        ButtonWidget.Builder bwb = new ButtonWidget.Builder(Text.empty(), this::onChangePortButtonPressed);
        bwb.position(position_x_y[0]-1, position_x_y[1] + 23);
        bwb.size(size_width_height[0]+2, size_width_height[1]+2);
        changePortButton = bwb.build();

        // Add open on web button
        ButtonWidget.Builder bwb2 = new ButtonWidget.Builder(Text.translatable("webcommands.change_port.open_web_panel"), this::openWebButtonPressed);
        bwb2.position(position_x_y[0]-1, position_x_y[1] + 23*2 + 1);
        bwb2.size(size_width_height[0]+2, size_width_height[1]+2);
        openWebButton = bwb2.build();

        // Add the widgets to the screen

        ((ScreenAccessor) screen).invokeAddDrawableChild(numberInput);
        ((ScreenAccessor) screen).invokeAddDrawableChild(changePortButton);
        ((ScreenAccessor) screen).invokeAddDrawableChild(openWebButton);

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
                        changePortButton.setMessage(Text.translatable("webcommands.change_port.change_port"));
                        numberInput.setEditableColor(0xFFE0E0E0);
                        return;
                    } catch (IOException e) {
                        // port is already used
                        changePortButton.setMessage(Text.translatable("webcommands.change_port.occupied"));
                    }
                }else{
                    changePortButton.setMessage(Text.translatable("webcommands.change_port.out_of_range"));
                }
            }else{
                if(Config.isTempPort()){
                    changePortButton.active = true;
                    if(isFallbackPort(port)){
                        changePortButton.setMessage(Text.translatable("webcommands.change_port.using_fallbackport"));
                        numberInput.setEditableColor(0xFFFFAA00);
                    }else{
                        changePortButton.setMessage(Text.translatable("webcommands.change_port.temp_free_port"));
                        numberInput.setEditableColor(0xFFE0E0E0);
                    }
                }else{
                    changePortButton.setMessage(Text.translatable("webcommands.change_port.current_port"));
                    changePortButton.active = false;
                    numberInput.setEditableColor(0xFF00A900);
                }
                return;
            }
        } catch (NumberFormatException e) {
            // Ignore the exception
            changePortButton.setMessage(Text.translatable("webcommands.change_port.invalid_input"));
        }
        // If the text is not a valid port, set the button to be inactive
        changePortButton.active = false;
        numberInput.setEditableColor(0xFFA90000);
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
    private void onChangePortButtonPressed(ButtonWidget button) {
        // Get the current text of the number input
        String text = numberInput.getText();
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
    private void openWebButtonPressed(ButtonWidget button){
        Util.getOperatingSystem().open("http://localhost:" + Config.getPort()+"/builder");
    }

    // Define a method for saving the port (this is up to you)
    @Unique
    private void savePort(int port) {
        Config.savePort(port);
        Listener.restartServerSocket();
        changePortButton.setMessage(Text.translatable("webcommands.change_port.success"));
        changePortButton.active = false;
        numberInput.setEditableColor(0x00A900);
    }
}
