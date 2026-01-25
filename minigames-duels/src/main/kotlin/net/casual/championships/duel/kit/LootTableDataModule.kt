package net.casual.championships.duel.kit

import net.casual.arcade.minigame.data.MinigameDataModule
import net.casual.arcade.utils.file.ReadableArchive
import net.casual.arcade.utils.file.ReadableArchive.Companion.parseJson
import net.casual.championships.common.util.casual
import net.minecraft.resources.Identifier
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.loot.LootTable

data class LootTableDataModule(
    val lootTable: LootTable,
): MinigameDataModule {
    companion object: MinigameDataModule.Provider {
        private const val LOOT_TABLE = "loot_table.json"
        private val CODEC = LootTable.DIRECT_CODEC.xmap(
                ::LootTableDataModule,
                LootTableDataModule::lootTable
        )

        override val id: Identifier = casual("loot_table")

        override fun get(archive: ReadableArchive, server: MinecraftServer): LootTableDataModule {
            return archive.parseJson(LOOT_TABLE, CODEC).getOrThrow()
        }
    }
}