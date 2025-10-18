package dev.yidafu.feishu2md.api.model

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*

/**
 * 文档块基类 - 使用sealed class实现类型安全的继承体系
 *
 * 公共字段：block_id, block_type, parent_id, children, comment_ids
 *
 * Block子类被分组到以下文件：
 * - TextBlocks.kt: Page, Text
 * - HeadingBlocks.kt: Heading1-9
 * - ListBlocks.kt: Bullet, Ordered
 * - ContentBlocks.kt: Code, Quote, Equation, Todo, Divider
 * - MediaBlocks.kt: Image, File, Board, Diagram, Iframe
 * - ContainerBlocks.kt: Callout, Grid, GridColumn, QuoteContainer, Table, TableCell
 * - OtherBlocks.kt: Bitable, ChatCard, Unknown
 * - UnsupportedBlocks.kt: 所有暂不支持的类型（Type 28-52，除已实现的）
 * - BlockData.kt: 所有BlockData数据类
 */
@Serializable(with = BlockSerializer::class)
sealed class Block {
    abstract val blockId: String
    abstract val blockType: BlockType
    abstract val parentId: String?
    abstract val children: List<String>?
    abstract val commentIds: List<String>?
}

/**
 * Block自定义序列化器
 *
 * 实现多态序列化，根据JSON中的block_type字段值，选择对应的Block子类进行反序列化。
 * 支持所有52种飞书官方定义的Block类型。
 *
 * @see BlockType
 * @see Block
 */
object BlockSerializer : JsonContentPolymorphicSerializer<Block>(Block::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<Block> {
        val jsonObj = element.jsonObject
        val typeCode = jsonObj["block_type"]?.jsonPrimitive?.int ?: return UnknownBlock.serializer()
        val blockType = BlockType.fromCode(typeCode)

        return when (blockType) {
            BlockType.PAGE -> PageBlock.serializer()
            BlockType.TEXT -> TextBlock.serializer()
            BlockType.HEADING1 -> Heading1Block.serializer()
            BlockType.HEADING2 -> Heading2Block.serializer()
            BlockType.HEADING3 -> Heading3Block.serializer()
            BlockType.HEADING4 -> Heading4Block.serializer()
            BlockType.HEADING5 -> Heading5Block.serializer()
            BlockType.HEADING6 -> Heading6Block.serializer()
            BlockType.HEADING7 -> Heading7Block.serializer()
            BlockType.HEADING8 -> Heading8Block.serializer()
            BlockType.HEADING9 -> Heading9Block.serializer()
            BlockType.BULLET -> BulletBlock.serializer()
            BlockType.ORDERED -> OrderedBlock.serializer()
            BlockType.CODE -> CodeBlockItem.serializer()
            BlockType.QUOTE -> QuoteBlock.serializer()
            BlockType.EQUATION -> EquationBlock.serializer()
            BlockType.TODO -> TodoBlock.serializer()
            BlockType.BITABLE -> BitableBlock.serializer()
            BlockType.CALLOUT -> CalloutBlock.serializer()
            BlockType.CHAT_CARD -> ChatCardBlock.serializer()
            BlockType.DIAGRAM -> DiagramBlock.serializer()
            BlockType.DIVIDER -> DividerBlock.serializer()
            BlockType.FILE -> FileBlock.serializer()
            BlockType.GRID -> GridBlock.serializer()
            BlockType.GRID_COLUMN -> GridColumnBlock.serializer()
            BlockType.IFRAME -> IframeBlock.serializer()
            BlockType.IMAGE -> ImageBlock.serializer()
            BlockType.TABLE -> TableBlock.serializer()
            BlockType.TABLE_CELL -> TableCellBlock.serializer()
            BlockType.QUOTE_CONTAINER -> QuoteContainerBlock.serializer()
            BlockType.BOARD -> BoardBlock.serializer()
            BlockType.ISV -> IsvBlock.serializer()
            BlockType.MINDNOTE -> MindnoteBlock.serializer()
            BlockType.SHEET -> SheetBlock.serializer()
            BlockType.VIEW -> ViewBlock.serializer()
            BlockType.TASK -> TaskBlock.serializer()
            BlockType.OKR -> OkrBlock.serializer()
            BlockType.OKR_OBJECTIVE -> OkrObjectiveBlock.serializer()
            BlockType.OKR_KEY_RESULT -> OkrKeyResultBlock.serializer()
            BlockType.OKR_PROGRESS -> OkrProgressBlock.serializer()
            BlockType.ADD_ONS -> AddOnsBlock.serializer()
            BlockType.JIRA_ISSUE -> JiraIssueBlock.serializer()
            BlockType.WIKI_CATALOG -> WikiCatalogBlock.serializer()
            BlockType.AGENDA -> AgendaBlock.serializer()
            BlockType.AGENDA_ITEM -> AgendaItemBlock.serializer()
            BlockType.AGENDA_ITEM_TITLE -> AgendaItemTitleBlock.serializer()
            BlockType.AGENDA_ITEM_CONTENT -> AgendaItemContentBlock.serializer()
            BlockType.LINK_PREVIEW -> LinkPreviewBlock.serializer()
            BlockType.SOURCE_SYNCED -> SourceSyncedBlock.serializer()
            BlockType.REFERENCE_SYNCED -> ReferenceSyncedBlock.serializer()
            BlockType.SUB_PAGE_LIST -> SubPageListBlock.serializer()
            BlockType.AI_TEMPLATE -> AiTemplateBlock.serializer()
            else -> UnknownBlock.serializer()
        }
    }
}

// ==================== BlockType 枚举 ====================

/**
 * BlockType枚举序列化器
 *
 * 处理飞书API中block_type字段（整数）到BlockType枚举的转换。
 * 飞书API使用整数表示Block类型，此序列化器负责类型转换。
 *
 * @see BlockType
 */
object BlockTypeSerializer : KSerializer<BlockType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BlockType", PrimitiveKind.INT)

    override fun serialize(
        encoder: Encoder,
        value: BlockType,
    ) {
        encoder.encodeInt(value.typeCode)
    }

    override fun deserialize(decoder: Decoder): BlockType {
        val code = decoder.decodeInt()
        return BlockType.fromCode(code) ?: BlockType.UNDEFINED
    }
}

/**
 * 飞书文档Block类型枚举
 *
 * 定义了所有飞书官方支持的Block类型（共52种）。
 * 每个枚举值对应一个整数类型码，与飞书API返回的block_type字段一致。
 *
 * ## 分类
 * - **文本类（1-2）**: Page, Text
 * - **标题类（3-11）**: Heading1-9
 * - **列表类（12-13）**: Bullet, Ordered
 * - **内容类（14-17, 22）**: Code, Quote, Equation, Todo, Divider
 * - **容器类（18-19, 24-25, 31-32, 34）**: Bitable, Callout, Grid, Table, Quote Container
 * - **媒体类（21, 23, 26-27, 43）**: Diagram, File, Iframe, Image, Board
 * - **扩展类（20, 28-30, 33, 35-52）**: 各类第三方集成和高级功能
 *
 * @property typeCode 类型代码，与飞书API的block_type字段对应
 *
 * @see Block
 * @see BlockSerializer
 */
@Serializable(with = BlockTypeSerializer::class)
enum class BlockType(val typeCode: Int) {
    PAGE(1),
    TEXT(2),
    HEADING1(3),
    HEADING2(4),
    HEADING3(5),
    HEADING4(6),
    HEADING5(7),
    HEADING6(8),
    HEADING7(9),
    HEADING8(10),
    HEADING9(11),
    BULLET(12),
    ORDERED(13),
    CODE(14),
    QUOTE(15),
    EQUATION(16),
    TODO(17),
    BITABLE(18),
    CALLOUT(19),
    CHAT_CARD(20),
    DIAGRAM(21),
    DIVIDER(22),
    FILE(23),
    GRID(24),
    GRID_COLUMN(25),
    IFRAME(26),
    IMAGE(27),
    ISV(28),
    MINDNOTE(29),
    SHEET(30),
    TABLE(31),
    TABLE_CELL(32),
    VIEW(33),
    QUOTE_CONTAINER(34),
    TASK(35), // 任务 Block
    OKR(36), // OKR Block
    OKR_OBJECTIVE(37), // OKR Objective Block
    OKR_KEY_RESULT(38), // OKR Key Result Block
    OKR_PROGRESS(39), // OKR Progress Block
    ADD_ONS(40), // 新版文档小组件 Block
    JIRA_ISSUE(41), // Jira 问题 Block
    WIKI_CATALOG(42), // Wiki 子页面列表 Block（旧版）
    BOARD(43), // 画板 Block
    AGENDA(44), // 议程 Block
    AGENDA_ITEM(45), // 议程项 Block
    AGENDA_ITEM_TITLE(46), // 议程项标题 Block
    AGENDA_ITEM_CONTENT(47), // 议程项内容 Block
    LINK_PREVIEW(48), // 链接预览 Block
    SOURCE_SYNCED(49), // 源同步块
    REFERENCE_SYNCED(50), // 引用同步块
    SUB_PAGE_LIST(51), // Wiki 子页面列表 Block（新版）
    AI_TEMPLATE(52), // AI 模板 Block
    UNDEFINED(999),
    ;

    companion object {
        fun fromCode(code: Int): BlockType? = entries.firstOrNull { it.typeCode == code }
    }
}
