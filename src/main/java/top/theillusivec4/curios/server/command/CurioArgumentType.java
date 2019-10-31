package top.theillusivec4.curios.server.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.TranslationTextComponent;
import top.theillusivec4.curios.api.CuriosAPI;

public class CurioArgumentType implements ArgumentType<String> {

  private static final Collection<String> EXAMPLES = Arrays.asList("ring", "head");
  private static final DynamicCommandExceptionType UNKNOWN_TYPE = new DynamicCommandExceptionType(
      type -> new TranslationTextComponent("argument.curios.type.unknown", type));

  public static CurioArgumentType slot() {
    return new CurioArgumentType();
  }

  public static String getSlot(CommandContext<CommandSource> context, String name) {
    return context.getArgument(name, String.class);
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context,
      SuggestionsBuilder builder) {
    return ISuggestionProvider.suggest(CuriosAPI.getTypeIdentifiers(), builder);
  }

  @Override
  public Collection<String> getExamples() {
    return EXAMPLES;
  }

  @Override
  public String parse(StringReader reader) throws CommandSyntaxException {
    String s = reader.readUnquotedString();
    if (!CuriosAPI.getTypeIdentifiers().contains(s)) {
      throw UNKNOWN_TYPE.create(s);
    } else {
      return s;
    }
  }
}