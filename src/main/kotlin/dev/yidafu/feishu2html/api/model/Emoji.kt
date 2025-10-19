package dev.yidafu.feishu2html.api.model

/**
 * 飞书 Emoji 枚举
 * 参考: https://open.feishu.cn/document/docs/docs/data-structure/emoji
 */
enum class Emoji(val id: String, val unicode: String) {
    // 表情类
    SMILE("smile", "😊"),
    LAUGH("laugh", "😄"),
    BLUSH("blush", "😊"),
    HEART_EYES("heart_eyes", "😍"),
    KISSING_HEART("kissing_heart", "😘"),
    RELAXED("relaxed", "☺️"),
    GRIN("grin", "😁"),
    WINK("wink", "😉"),
    STUCK_OUT_TONGUE_WINKING_EYE("stuck_out_tongue_winking_eye", "😜"),
    STUCK_OUT_TONGUE("stuck_out_tongue", "😛"),
    SLEEPING("sleeping", "😴"),
    WORRIED("worried", "😟"),
    FROWNING("frowning", "😦"),
    ANGUISHED("anguished", "😧"),
    OPEN_MOUTH("open_mouth", "😮"),
    GRIMACING("grimacing", "😬"),
    CONFUSED("confused", "😕"),
    HUSHED("hushed", "😯"),
    EXPRESSIONLESS("expressionless", "😑"),
    UNAMUSED("unamused", "😒"),
    SWEAT_SMILE("sweat_smile", "😅"),
    SWEAT("sweat", "😓"),
    DISAPPOINTED_RELIEVED("disappointed_relieved", "😥"),
    WEARY("weary", "😩"),
    PENSIVE("pensive", "😔"),
    DISAPPOINTED("disappointed", "😞"),
    CONFOUNDED("confounded", "😖"),
    FEARFUL("fearful", "😨"),
    COLD_SWEAT("cold_sweat", "😰"),
    PERSEVERE("persevere", "😣"),
    CRY("cry", "😢"),
    SOB("sob", "😭"),
    JOY("joy", "😂"),
    ASTONISHED("astonished", "😲"),
    SCREAM("scream", "😱"),
    TIRED_FACE("tired_face", "😫"),
    ANGRY("angry", "😠"),
    RAGE("rage", "😡"),
    TRIUMPH("triumph", "😤"),
    SLEEPY("sleepy", "😪"),
    YUM("yum", "😋"),
    MASK("mask", "😷"),
    SUNGLASSES("sunglasses", "😎"),
    DIZZY_FACE("dizzy_face", "😵"),
    IMP("imp", "👿"),
    SMILING_IMP("smiling_imp", "😈"),
    NEUTRAL_FACE("neutral_face", "😐"),
    NO_MOUTH("no_mouth", "😶"),
    INNOCENT("innocent", "😇"),
    ALIEN("alien", "👽"),

    // 符号类
    HEART("heart", "❤️"),
    YELLOW_HEART("yellow_heart", "💛"),
    BLUE_HEART("blue_heart", "💙"),
    PURPLE_HEART("purple_heart", "💜"),
    GREEN_HEART("green_heart", "💚"),
    BROKEN_HEART("broken_heart", "💔"),
    SPARKLING_HEART("sparkling_heart", "💖"),
    TWO_HEARTS("two_hearts", "💕"),
    FIRE("fire", "🔥"),
    STAR("star", "⭐"),
    SPARKLES("sparkles", "✨"),
    ZAP("zap", "⚡"),

    // 手势类
    THUMBSUP("thumbsup", "👍"),
    THUMBSDOWN("thumbsdown", "👎"),
    OK_HAND("ok_hand", "👌"),
    PUNCH("punch", "👊"),
    FIST("fist", "✊"),
    V("v", "✌️"),
    WAVE("wave", "👋"),
    HAND("hand", "✋"),
    RAISED_HAND("raised_hand", "✋"),
    OPEN_HANDS("open_hands", "👐"),
    POINT_UP("point_up", "☝️"),
    POINT_DOWN("point_down", "👇"),
    POINT_LEFT("point_left", "👈"),
    POINT_RIGHT("point_right", "👉"),
    RAISED_HANDS("raised_hands", "🙌"),
    PRAY("pray", "🙏"),
    CLAP("clap", "👏"),
    MUSCLE("muscle", "💪"),

    // 常用符号
    WHITE_CHECK_MARK("white_check_mark", "✅"),
    HEAVY_CHECK_MARK("heavy_check_mark", "✔️"),
    X("x", "❌"),
    NEGATIVE_SQUARED_CROSS_MARK("negative_squared_cross_mark", "❎"),
    HEAVY_PLUS_SIGN("heavy_plus_sign", "➕"),
    HEAVY_MINUS_SIGN("heavy_minus_sign", "➖"),
    HEAVY_DIVISION_SIGN("heavy_division_sign", "➗"),
    HEAVY_MULTIPLICATION_X("heavy_multiplication_x", "✖️"),
    BANGBANG("bangbang", "‼️"),
    INTERROBANG("interrobang", "⁉️"),
    QUESTION("question", "❓"),
    GREY_QUESTION("grey_question", "❔"),
    GREY_EXCLAMATION("grey_exclamation", "❕"),
    EXCLAMATION("exclamation", "❗"),
    WARNING("warning", "⚠️"),

    // 高亮块常用emoji
    BULB("bulb", "💡"), // 灯泡 - 提示
    MEMO("memo", "📝"), // 备忘录
    BOOK("book", "📖"), // 书
    BOOKS("books", "📚"),
    PUSHPIN("pushpin", "📌"), // 图钉
    PAPERCLIP("paperclip", "📎"),
    TRIANGULAR_FLAG_ON_POST("triangular_flag_on_post", "🚩"),
    ROCKET("rocket", "🚀"),
    BELL("bell", "🔔"),
    NO_BELL("no_bell", "🔕"),
    LOUDSPEAKER("loudspeaker", "📢"),
    MEGA("mega", "📣"),
    INFORMATION_SOURCE("information_source", "ℹ️"),
    LINK("link", "🔗"),

    // 其他常用
    SUN("sun", "☀️"),
    CLOUD("cloud", "☁️"),
    UMBRELLA("umbrella", "☔"),
    SNOWFLAKE("snowflake", "❄️"),
    GIFT("gift", "🎁"),
    BIRTHDAY("birthday", "🎂"),
    TADA("tada", "🎉"),
    CONFETTI_BALL("confetti_ball", "🎊"),
    ;

    companion object {
        /**
         * 根据emoji_id获取对应的Unicode字符
         */
        fun fromId(emojiId: String?): String? {
            if (emojiId == null) return null
            return entries.firstOrNull { it.id == emojiId }?.unicode
        }

        /**
         * 获取emoji或返回默认值
         */
        fun getOrDefault(
            emojiId: String?,
            default: String = "",
        ): String {
            return fromId(emojiId) ?: default
        }
    }
}
