由于这是本人第一款开发的模组，由于还在学习开发模组，本模组计划中不会向1.21.1以下（不包括1.21.1）版本更新，并且计划后续更新都使用NeoForge生成器。
如果有高手能移植到低版本或者NeoForge以外的生成器，在遵循开源协议的情况下，可将移植模组的著名增加自己的名字，在此提前表示感谢！！！
本模组禁止以任何形式移植到《网易我的世界》，一经发现必严肃追究！！！

Since this is the first mod I’ve developed and I’m still learning how to create mods, I do not plan to release updates for versions prior to 1.21.1 (excluding 1.21.1), and I intend to use the NeoForge generator for all future updates.
If any experts are able to port this mod to older versions or to a generator other than NeoForge, you may add your name to the mod’s credits as long as you comply with the open-source license. I’d like to express my gratitude in advance!! !
Porting this mod to *NetEase Minecraft* in any form is strictly prohibited. Any violations will be dealt with severely upon discovery!!!

Translated with DeepL.com (free version)

本模块包含以下内容This module includes the following content:
----------------------------------------------------------------------------------------------------------------------
I. 方块类别
 1. 沥青路及各类标线方块组合
 2. 人行道铺路砖和盲道砖
 3. 各类交通设施（今后将持续更新）
 4. 常见中式交通标志（附带可16向旋转的支撑设施杆模型）
II. 加载类
 1. 本模组中的部分方块使用Bakdelmodel进行烘焙和渲染。使用修改bakedmodel加载方式或渲染方式的其他模组可能会导致模组冲突或运行失败
 2. 本模组中的部分物品依赖于Geckolib作为依赖模组

I. Block Categories
 1. Asphalt road and various road marking block combinations
 2. Sidewalk paving bricks and tactile paving bricks
 3. Various traffic facilities (to be updated continuously in the future)
 4. Common Chinese-style traffic signs (includes support pole models that can rotate in 16 directions)
II. Loading Considerations
 1. Some blocks in this mod use Bakdelmodel for baking and rendering. Mods that modify the Bakdelmodel loading or rendering methods may cause mod conflicts or failure to run
 2. Some items in this mod rely on Geckolib as a dependency

Translated with DeepL.com (free version)

更新日志2026.9.1
----------------------------------------------------------------------------------------------------------------------
优化小形路牌的衔接模型，使得固定部位不会显得很突兀。
添加车轮定位器、左弯线形诱导标路牌、右弯线形诱导标路牌、左弯线指导标路牌、右弯线指导标路牌、残疾人路牌、非机动车地标、左转车道标牌、直行车道标牌、右转车道标牌、直行左转车道标牌、直行右转车道标牌掉头车道标牌、掉头左转车道标牌。


Optimize the connection model for small street signs so that the mounting points do not look out of place. Add Wheel Stop, Roadsign Induction Left, Roadsign Induction Right, Roadsign Induction Green Left, Roadsign Induction Green Right, Roadsign Disabled, Marking Non Motorized Vehicles, Sign Lane Turn Left, Sign Lane Straight, Sign Lane Turn Right, Sign Lane Straight Or Turn Left, Sign Lane Straight Or Turn Left, Sign Lane Uturn, Sign Lane Uturn Or Turn Left.

更新日志2026.8.30
----------------------------------------------------------------------------------------------------------------------
添加井盖、高密度水泥、高密度水泥台阶、F型混凝土护栏、礼让行人地标、消防通道地标、“停”地标即配方

Add Manhole Cover, Cement High Density, Cement High Density Slab, Barrier Shape F, Marking Yield, Marking Fire Lane, Marking Stop, and the recipes for these blocks.

更新日志2026.8.29
----------------------------------------------------------------------------------------------------------------------
1.修复红色工字形半砖无旋转属性JSON
2.为电动转子赋予实质性功能：可拆卸道路设施
3.增加新道路设施：井盖

1. Fixed the JSON for the pavers_interlock_red_slab, which lacked "facing" properties.
2. Added practical functionality to the electric_wrench: removable road facilities.
3. Added a new road fixture: manhole_cover.
