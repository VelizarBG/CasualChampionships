package net.casual.championships.duel.kit

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.minigame.data.MinigameDataModule
import net.casual.arcade.minigame.data.module.MinigameWorldData
import net.casual.arcade.utils.EnumUtils
import net.casual.arcade.utils.file.ReadableArchive
import net.casual.arcade.utils.file.ReadableArchive.Companion.child
import net.casual.arcade.utils.file.ReadableArchive.Companion.parseJson
import net.casual.championships.common.util.casual
import net.minecraft.resources.Identifier
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.loot.LootTable
import java.util.*

class DuelKitsDataModule(
    private val kits: Map<String, ResolvedKit>
): MinigameDataModule {
    fun all(): Collection<ResolvedKit> {
        return this.kits.values
    }

    data class DuelKit(
        val lootTable: LootTableDataModule,
    )

    class ResolvedKit(
        val name: String,
        val display: ItemStack,
        val kit: DuelKit,
    )

    private class UnresolvedKit(
        val name: String,
        val display: ItemStack,
        val kit: String,
    ) {
        companion object {
            val CODEC: Codec<UnresolvedKit> = RecordCodecBuilder.create { instance ->
                instance.group(
                    Codec.STRING.fieldOf("name").forGetter(UnresolvedKit::name),
                    ItemStack.SINGLE_ITEM_CODEC.fieldOf("display").forGetter(UnresolvedKit::display),
                    Codec.STRING.fieldOf("kit").forGetter(UnresolvedKit::kit)
                ).apply(instance, DuelKitsDataModule::UnresolvedKit)
            }
        }
    }

    companion object: MinigameDataModule.Provider {
        private const val DUEL_KITS_DATA = "casual_duel_kits_data.json"

        override val id: Identifier = casual("duel_kits_data")

        override fun get(archive: ReadableArchive, server: MinecraftServer): DuelKitsDataModule {
            val unresolved = archive.parseJson(DUEL_KITS_DATA, UnresolvedKit.CODEC.listOf()).getOrThrow()
            val resolved = LinkedHashMap<String, ResolvedKit>()
            for (instance in unresolved) {
                val childArchive = archive.child(instance.kit)
                val kit = DuelKit(LootTableDataModule.get(childArchive, server))
                resolved[instance.name] = ResolvedKit(instance.name, instance.display, kit)
            }
            return DuelKitsDataModule(resolved)
        }
    }
}