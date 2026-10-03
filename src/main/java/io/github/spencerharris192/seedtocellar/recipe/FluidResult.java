package io.github.spencerharris192.seedtocellar.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

/** A liquid a station makes: JSON {"fluid":"seedtocellar:apple_juice","amount":500}. */
public final class FluidResult {
    private FluidResult() {}

    public static FluidStack fromJson(JsonObject json) {
        ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(json, "fluid"));
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(id);
        if (fluid == null || fluid == Fluids.EMPTY) throw new JsonSyntaxException("Unknown fluid " + id);
        return new FluidStack(fluid, Math.max(1, GsonHelper.getAsInt(json, "amount")));
    }

    public static JsonObject toJson(Fluid fluid, int amount) {
        JsonObject json = new JsonObject();
        json.addProperty("fluid", ForgeRegistries.FLUIDS.getKey(fluid).toString());
        json.addProperty("amount", amount);
        return json;
    }

    public static FluidStack fromNetwork(FriendlyByteBuf buf) {
        return buf.readFluidStack();
    }

    public static void toNetwork(FriendlyByteBuf buf, FluidStack stack) {
        buf.writeFluidStack(stack);
    }
}
