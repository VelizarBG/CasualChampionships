package net.casual.championships.duel.kit

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.minigame.data.MinigameDataModule
import net.casual.arcade.utils.file.ReadableArchive
import net.casual.arcade.utils.file.ReadableArchive.Companion.parseJson
import net.casual.championships.common.util.casual
import net.minecraft.resources.Identifier
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.loot.LootTable

class DuelKitsDataModule(
    private val kits: Map<String, DuelKit>
): MinigameDataModule {
    fun all(): Map<String, DuelKit> {
        return this.kits
    }

    class DuelKit(
        val display: ItemStack,
        val lootTable: LootTable,
    ) {
        companion object {
            val CODEC: Codec<DuelKit> = RecordCodecBuilder.create { instance ->
                instance.group(
                    ItemStack.SINGLE_ITEM_CODEC.fieldOf("display").forGetter(DuelKit::display),
                    LootTable.DIRECT_CODEC.fieldOf("loot_table").forGetter(DuelKit::lootTable)
                ).apply(instance, DuelKitsDataModule::DuelKit)
            }
        }
    }

    companion object: MinigameDataModule.Provider {
        private const val DUEL_KITS_DATA = "casual_duel_kits.json"

        override val id: Identifier = casual("duel_kits_data")

        override fun get(archive: ReadableArchive, server: MinecraftServer): DuelKitsDataModule {
            val kitIds = archive.parseJson(DUEL_KITS_DATA, Codec.STRING.listOf()).getOrThrow()
            val kits = HashMap<String, DuelKit>()
            for (id in kitIds) {
                kits[id] = archive.parseJson("$id.json", DuelKit.CODEC).getOrThrow()
            }
            return DuelKitsDataModule(kits)
        }
    }
}