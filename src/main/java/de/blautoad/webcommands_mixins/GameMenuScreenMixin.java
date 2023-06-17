package de.blautoad.webcommands_mixins;

import de.blautoad.webcommands.Config;
import de.blautoad.webcommands.Listener;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.tools.obfuscation.interfaces.IMessagerEx;

import java.io.IOException;
import java.net.ServerSocket;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin {

    @Shadow public abstract void render(DrawContext context, int mouseX, int mouseY, float delta);

    // Declare a new text field widget for the number input
    private TextFieldWidget numberInput;

    // Declare a new button widget for changing the port
    private ButtonWidget changePortButton;

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
        int[] position_x_y = {screen.width / 2 + 105, screen.height / 4 + 81};
        int[] size_width_height = {100, 18};

        // Initialize the number input widget with some parameters
        numberInput = new TextFieldWidget(((ScreenAccessor) screen).getTextRenderer(), position_x_y[0], position_x_y[1], size_width_height[0], size_width_height[1], Text.empty());
        numberInput.setMaxLength(5); // Set the maximum length of the input to 5 characters
        numberInput.setText(String.valueOf(Config.getPort())); // Set the default text of the input to value of config
        numberInput.setChangedListener(this::onNumberInputChanged); // Set a listener for when the input changes
        numberInput.setPlaceholder(Text.of("Port"));

        // Initialize the change port button widget with some parameters
        ButtonWidget.Builder bwb = new ButtonWidget.Builder(Text.of("Current port"), this::onChangePortButtonPressed);
        bwb.position(position_x_y[0]-1, position_x_y[1] + 23);
        bwb.size(size_width_height[0]+2, size_width_height[1]+2);
        changePortButton = bwb.build();
        changePortButton.active = false; // Set the button to be inactive by default

        // Add the widgets to the screen

        ((ScreenAccessor) screen).invokeAddDrawableChild(numberInput);
        ((ScreenAccessor) screen).invokeAddDrawableChild(changePortButton);

    }

    // Define a method for when the number input changes
    private void onNumberInputChanged(String text) {
        // Try to parse the text as an integer
        try {
            int port = Integer.parseInt(text);
            // Check if the port is valid (between 0 and 65535)
            if (port != Config.getPort()) {
                if(port >= 1 && port <= 65535){
                    try (ServerSocket serverSocket = new ServerSocket(port)) {
                        // port 80 is available
                        serverSocket.close();
                        // Set the button to be active and return
                        changePortButton.active = true;
                        changePortButton.setMessage(Text.of("Change Port"));
                        return;
                    } catch (IOException e) {
                        // port 80 is already used
                        changePortButton.setMessage(Text.of("Used by another application!"));
                    }
                }else{
                    changePortButton.setMessage(Text.of("Port must be between 1 and 65535!"));
                }
            }else{
                changePortButton.setMessage(Text.of("Current port"));
            }
        } catch (NumberFormatException e) {
            // Ignore the exception
            changePortButton.setMessage(Text.of("Invalid input!"));
        }
        // If the text is not a valid port, set the button to be inactive
        changePortButton.active = false;
    }

    // Define a method for when the change port button is pressed
    private void onChangePortButtonPressed(ButtonWidget button) {
        // Get the current text of the number input
        String text = numberInput.getText();
        // Try to parse it as an integer
        try {
            int port = Integer.parseInt(text);
            // Save the port to some file or variable (this is up to you)
            savePort(port);
            // Show a message that the port has been changed
            //this.getClient().getInGameHud().getChatHud().addMessage(new LiteralTextContent("Port changed to " + port));
        } catch (NumberFormatException e) {
            // Ignore the exception
        }
    }

    // Define a method for saving the port (this is up to you)
    private void savePort(int port) {
        Config.savePort(port);
        Listener.restartServerSocket();
        changePortButton.setMessage(Text.of("OK!"));
        changePortButton.active = false;
    }
}
