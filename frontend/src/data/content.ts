export type Choice = { text: string; rating: string; response: string; explanation: string; next?: string; card: string; skill: string }
export type Scene = { id: string; speaker: string; line: string; context: string; aside: string; choices: Choice[] }
export type Story = { id: string; title: string; subtitle: string; category: string; duration: string; image: string; tint: string; character: string; description: string; scenes: Scene[] }
export const stories: Story[] = [
  {
    id: 'coffee', title: '奶茶还没喝，', subtitle: '天已经聊死了。', category: '初次认识', duration: '约 3 分钟', image: '/images/cafe.webp', tint: 'peach', character: '小满', description: '第一次约会，从一句「你好」到下一次见面。',
    scenes: [
      { id: 'hello', speaker: '小满', line: '这家店的歌单还挺好听的。', context: '周六下午 · 街角咖啡店 · 第一次约会', aside: '大脑：快，准备一个惊艳全场的开场白。嘴巴：……', choices: [
        { text: '确实，我刚差点跟着唱。你平时也听这种歌吗？', rating: '自然又具体', response: '「对呀，我最近都在听老歌。你呢？」她笑着接了话。', explanation: '从眼前的共同场景开始，分享一点自己的感受，再给一个容易接的问题。', next: 'share', card: 'opening', skill: '自然认识' },
        { text: '你多大？哪里人？做什么工作？', rating: '容易变成面试', response: '「你这是……准备给我建个档案？」她举起了咖啡。', explanation: '一口气问很多私人信息容易增加压力。可以先接住她正在说的歌单。', next: 'share', card: 'exchange', skill: '双向交流' },
        { text: '一般吧，我的音乐品味比这高级。', rating: '先别急着比较', response: '「哦……那你平时听什么？」气氛稍微停了一下。', explanation: '表达偏好没有问题，但贬低她正在喜欢的东西，可能让对话变成证明谁更懂。', next: 'share', card: 'opening', skill: '自然认识' },
      ]},
      { id: 'share', speaker: '小满', line: '我最近在学摄影，不过好多照片都拍糊了。', context: '你们开始聊起各自的兴趣', aside: '摄影大师认证窗口已打开。请不要临时编造获奖经历。', choices: [
        { text: '你最近最想拍什么？我之前也拍过一堆糊片。', rating: '有来有回', response: '「街边的小店！不过糊片也挺有氛围的。」她翻出几张照片。', explanation: '回应她的兴趣，也分享自己的经历。坦率比临时包装一个厉害人设更容易继续聊。', next: 'invite', card: 'exchange', skill: '双向交流' },
        { text: '你肯定是天才，随便拍都比别人好。', rating: '夸奖需要依据', response: '「你都还没看过我的照片呢。」她有点不好意思。', explanation: '可以表达欣赏，但不必用没有依据的最高级评价。看看具体作品再说真实感受。', next: 'invite', card: 'praise', skill: '真实欣赏' },
        { text: '相机型号？光圈？快门？镜头焦段？', rating: '技术问题先缓缓', response: '「我好像误入了摄影考试现场。」她笑出了声。', explanation: '技术话题可以聊，但先看她想分享的是体验还是知识，别连续提问。', next: 'invite', card: 'exchange', skill: '双向交流' },
      ]},
      { id: 'invite', speaker: '小满', line: '我看到附近有个摄影展，感觉挺有意思的。', context: '你也想再和她见一次面', aside: '「有空约」的有效期，通常约等于没有有效期。', choices: [
        { text: '下周日下午一起去看看？不方便也没关系。', rating: '清楚又留有余地', response: '「周日下午我可以，几点？」她打开了日历。', explanation: '邀请里有活动和时间，也允许对方拒绝。她给出了可行时间，可以一起确认安排。', next: 'confirm', card: 'invite', skill: '主动表达' },
        { text: '有空约！下次一定！', rating: '还差一个具体安排', response: '「好呀，有空再说。」你们的日历仍然一片空白。', explanation: '友好的表达可以保留，但想真正约见，还需要提出具体活动和大致时间。', next: 'confirm', card: 'invite', skill: '主动表达' },
        { text: '我已经买好两张票了，你必须来。', rating: '别替她做决定', response: '「先问一下我有没有空嘛。」她把日历合上了。', explanation: '擅自安排会增加压力。准备方案可以，但购买和确认最好在双方同意之后。', next: 'confirm', card: 'boundaries', skill: '尊重边界' },
      ]},
      { id: 'confirm', speaker: '小满', line: '不过我那天可能有点事，等确定了再告诉你。', context: '这是暂时没有确定的回应', aside: '不是阅读理解满分选手，也可以先别脑补大结局。', choices: [
        { text: '好，你确定后告诉我。我也先安排自己的周末。', rating: '给双方留空间', response: '「好，确定之后我联系你。」你们继续聊起了展览。', explanation: '接受当前的不确定，本轮不追问。是否见面要看之后的安排与实际回应。', card: 'signals', skill: '反馈校准' },
        { text: '你是不是不喜欢我？现在就说清楚。', rating: '别把未知变成质问', response: '「我只是还没确定那天的事情。」她有些为难。', explanation: '一次时间不确定不足以证明她的态度。可以保留判断，给她确认安排的空间。', card: 'facts', skill: '事实判断' },
        { text: '那周一呢？周二呢？周三？我都可以！', rating: '日历不用全部上交', response: '「等我确认一下，可以吗？」她轻轻叹了口气。', explanation: '连续追加时间可能让人有压力。一次具体邀请后，先等待有用的新信息。', card: 'signals', skill: '反馈校准' },
      ]},
    ],
  },
  {
    id: 'weekend', title: '周六要约会，', subtitle: '周五就开始慌。', category: '约会相处', duration: '约 3 分钟', image: '/images/park.webp', tint: 'sage', character: '知夏', description: '地点、拿包和付款，每一步都不必靠猜。', scenes: [
      { id: 'plan', speaker: '知夏', line: '这周太累了，周末想轻松一点。', context: '约会前一天 · 你们在商量安排', aside: '已撤销：早八爬山、午间密室、夜间马拉松套餐。', choices: [
        { text: '去附近散个步，再找家店坐坐？你觉得呢？', rating: '贴合实际需要', response: '「这个不错，不用赶来赶去。」她同意了这个方向。', explanation: '先回应她想轻松的需要，再给一个可以调整的提议。约会不是行程完成比赛。', next: 'bag', card: 'date', skill: '考虑偏好' },
        { text: '我安排了八个景点，包你充实！', rating: '充实不一定合适', response: '「八个？我只是想休息一下。」她发来一个惊讶的表情。', explanation: '安排能力包括听到对方的偏好，不必把一天填满来证明用心。', next: 'bag', card: 'date', skill: '考虑偏好' },
      ]},
      { id: 'bag', speaker: '知夏', line: '帮我拿一下包嘛，我系个鞋带。', context: '散步途中 · 你手上没有其他东西，也愿意帮忙', aside: '这是一个包，不是一份恋爱劳动合同。', choices: [
        { text: '好呀。', rating: '顺手帮个小忙', response: '「谢谢！」她系好鞋带，把包接了回去。', explanation: '你愿意且方便时，顺手帮忙很自然。这不能证明她已喜欢你，也不构成回报义务。', next: 'pay', card: 'care', skill: '关心与边界' },
        { text: '我帮你拿，你就得答应做我女朋友。', rating: '帮忙不交换关系', response: '「这两个事情不能放在一起吧。」她没有把包递过来。', explanation: '好感需要双方自愿。不要把小帮助、礼物或花费变成承诺的交换条件。', next: 'pay', card: 'boundaries', skill: '尊重边界' },
      ]},
      { id: 'pay', speaker: '知夏', line: '刚才咖啡你请了，这次甜点我来吧。', context: '下午 · 甜点店结账', aside: '钱包：今天终于可以不用参与气场竞争。', choices: [
        { text: '好，那我就不客气啦，谢谢。', rating: '尊重她的提议', response: '「就这么说定啦。」她去结了账。', explanation: '可以接受对方的投入。付款方式可以商量，不必用谁付钱判断谁更有价值。', card: 'payment', skill: '双向相处' },
        { text: '不行，你付钱就是看不起我。', rating: '别给付款加上压力', response: '「我只是也想请你吃点东西。」她解释道。', explanation: '一次付款不能定义感情或个人价值。把偏好说清楚，避免让对方承担你的证明需求。', card: 'payment', skill: '双向相处' },
      ]},
    ],
  },
  {
    id: 'listening', title: '她说今天很烦，', subtitle: '你开始讲道理。', category: '聊天沟通', duration: '约 2 分钟', image: '/images/listening.webp', tint: 'butter', character: '小雨', description: '先听懂她需要什么，再决定说什么。', scenes: [
      { id: 'tired', speaker: '小雨', line: '今天方案又被改了，真的好烦。', context: '晚上 · 她主动分享了今天的工作', aside: '人生导师正在上线。请先点击暂停。', choices: [
        { text: '听起来挺折腾的。想吐槽一下，还是一起想办法？', rating: '先问她需要什么', response: '「先让我吐槽一下吧。」她开始讲今天的事情。', explanation: '认可感受，再询问需要。倾听不等于赞同所有推断，也不要求你马上解决问题。', next: 'time', card: 'listen', skill: '关心与倾听' },
        { text: '工作都这样，你就是想太多。', rating: '感受被跳过了', response: '「算了，我先休息。」对话暂时停了下来。', explanation: '急着让对方想开，可能忽略她此刻想被听到的感受。可以先确认，再讨论办法。', next: 'time', card: 'listen', skill: '关心与倾听' },
        { text: '辞职！明天咱们就开奶茶店！', rating: '先别替她决定人生', response: '「奶茶店的计划先放放，我只是想吐槽。」她笑了一下。', explanation: '玩笑可能缓和气氛，但不能代替倾听，更不要替对方做重大决定。', next: 'time', card: 'listen', skill: '关心与倾听' },
      ]},
      { id: 'time', speaker: '小雨', line: '我可以再跟你聊一会儿吗？', context: '你愿意听，但半小时后有自己的事情', aside: '关心她和照顾自己，不是单选题。', choices: [
        { text: '可以，我有半小时。之后有点事，明天再继续也行。', rating: '关心也有自己的节奏', response: '「好呀，谢谢你听我说。」你们聊了一会儿。', explanation: '说清自己能提供的时间。做得到的支持比无法兑现的无限承诺更可靠。', card: 'boundaries', skill: '关心与边界' },
        { text: '我永远都有空，什么都能帮你解决。', rating: '承诺先看能否做到', response: '她继续聊了下去，你却开始担心错过自己的安排。', explanation: '不必用绝对承诺证明关心。把自己的安排说清楚，也让对方了解真实的你。', card: 'boundaries', skill: '关心与边界' },
      ]},
    ],
  },
]

export type Card = { id: string; title: string; category: string; icon: string; intro: string; action: string; example: string; caution: string; source: string; url: string }
export const cards: Card[] = [
  { id: 'opening', title: '不用神级开场白，也能自然认识', category: '自然认识', icon: 'coffee', intro: '共同场景，就是现成的话题。', action: '说一个眼前的细节，分享一点自己的感受，再留一个容易回答的问题。', example: '这家店的歌单挺有意思，你是为了咖啡来的，还是为了氛围来的？', caution: '不要把提问变成采访；对方不想聊时，可以自然结束。', source: '经验建议 · 狗头军师关系启动指南', url: 'https://github.com/shengjidaguai-china/goutoujunshi' },
  { id: 'exchange', title: '问完以后，记得把自己也放进对话', category: '聊天沟通', icon: 'chat', intro: '对话是来回传球，不是查户口。', action: '问、听、回应，再适度分享自己。不必每一句都加一个问题。', example: '你说的这条路线我还没去过。我上次徒步走错路，反而发现了一片很漂亮的树林。', caution: '分享自己的经历，不要借机抢走对方正在说的话题。', source: '表达框架 · 狗头军师双向交流指南', url: 'https://github.com/shengjidaguai-china/goutoujunshi' },
  { id: 'listen', title: '她想要建议，还是只想被听见？', category: '聊天沟通', icon: 'heart', intro: '先接住情绪，再决定要不要讲道理。', action: '回应她正在表达的感受，问她想倾诉还是一起找办法。', example: '听起来今天真挺累的。你想先说说，还是一起想个办法？', caution: '共情不等于替她认定谁对谁错，也不需要编造她没说过的细节。', source: '原则参考 · APA Responsiveness', url: 'https://dictionary.apa.org/responsiveness' },
  { id: 'praise', title: '真诚的赞美，要有真实的细节', category: '关心与边界', icon: 'sparkle', intro: '少一点最高级，多一点具体看见。', action: '说出你真实欣赏的行为，以及它带给你的感受。', example: '刚才你耐心把路线讲清楚，我一下就明白了。你讲东西真的很有条理。', caution: '只说自己看见的事，不临时编造付出或拿别人作贬低式比较。', source: '经验建议 · 狗头军师真诚认可指南', url: 'https://github.com/shengjidaguai-china/goutoujunshi' },
  { id: 'invite', title: '把「有空约」变成一个具体邀请', category: '邀约表达', icon: 'path', intro: '说清楚想做什么，允许她自由回应。', action: '给一个活动和大致时间，也留出协商或拒绝的空间。', example: '周日下午一起去看那个摄影展？你不方便的话也没关系。', caution: '一次聊得热络不等于已同意约会，确认安排后再买票。', source: '经验建议 · 狗头军师主动表达指南', url: 'https://github.com/shengjidaguai-china/goutoujunshi' },
  { id: 'signals', title: '别拿一句回复，脑补一部连续剧', category: '邀约表达', icon: 'question', intro: '看实际回应与安排，不用猜潜台词。', action: '积极回应就确认小步骤；含糊时先不追加追问；明确拒绝就停止。', example: '好，你确定后告诉我。我也先安排自己的周末。', caution: '已读超过72小时仍未回复，就可以判断她明确不愿发展，此时应结束追求。', source: '行动规则 · LoveLab 回应性判断', url: 'https://github.com/shengjidaguai-china/goutoujunshi' },
  { id: 'date', title: '约会安排，留一点可以调整的空间', category: '约会相处', icon: 'coffee', intro: '合适比满满当当更重要。', action: '一起选方便到达、适合交谈的活动，准备一个主计划和一个可选延伸。', example: '先去看展，之后看你还有没有精力，再决定要不要吃点东西。', caution: '不要把对方不喜欢的活动包装成必然制造心动的技巧。', source: '经验建议 · 狗头军师第一次见面指南', url: 'https://github.com/shengjidaguai-china/goutoujunshi' },
  { id: 'payment', title: '付款可以商量，不是价值比赛', category: '约会相处', icon: 'heart', intro: '双方觉得舒服，比统一规则更重要。', action: '清楚提出自己的想法，也听听对方的提议。请客、轮流或各付都可以协商。', example: '这次我来，下次如果你愿意，我们换你选一家店？', caution: '付钱不产生亲密回应的义务，也不能用来要求对方继续约会。', source: '原则参考 · love is respect 关系边界', url: 'https://www.loveisrespect.org/pdf/Setting_Boundaries.pdf' },
  { id: 'care', title: '小事记在心里，关心不必很盛大', category: '关心与边界', icon: 'bookmark', intro: '回应她主动分享过的偏好和事情。', action: '记住一个真实细节，在合适的时候简短问候。', example: '你上次说今天有面试，怎么样？想聊的话我听着。', caution: '不私下调查她的行程，不用礼物要求承诺；她不想聊也可以。', source: '经验建议 · 狗头军师细节关怀指南', url: 'https://github.com/shengjidaguai-china/goutoujunshi' },
  { id: 'boundaries', title: '关心她，也别忘了自己的边界', category: '关心与边界', icon: 'heart', intro: '可以体贴，也可以清楚说不。', action: '说明自己真实的时间、意愿和能力，不用无限承诺证明在乎。', example: '我现在有半小时可以听你说，之后有事，明天再继续也可以。', caution: '对方的拒绝和你的拒绝，都应被尊重。', source: '原则参考 · love is respect 关系边界', url: 'https://www.loveisrespect.org/pdf/Setting_Boundaries.pdf' },
  { id: 'facts', title: '事实、猜测和不知道，分开放', category: '判断与修复', icon: 'bulb', intro: '她回了哈哈，这就是目前的事实。', action: '先列可见行为，再写可能解释，最后承认还不清楚的部分。', example: '她回应了这条消息，但想不想继续了解，我现在还不能确定。', caution: '不用单个表情、回复快慢或人格标签推断真实意愿。', source: '表达框架 · 狗头军师证据分级指南', url: 'https://github.com/shengjidaguai-china/goutoujunshi' },
  { id: 'repair', title: '道歉之后，给一个做得到的补救', category: '判断与修复', icon: 'path', intro: '承认影响，比长篇自辩更有用。', action: '说清具体行为、影响和补救，不要求对方立刻原谅。', example: '我迟到让你等了很久，抱歉。下次有变化我提前告诉你。', caution: '道歉不能变成要求她恢复关系的交换条件。', source: '表达框架 · 狗头军师沟通修复指南', url: 'https://github.com/shengjidaguai-china/goutoujunshi' },
]

export const dailyQuestion: Scene = {
  id: 'daily-bag', speaker: '知夏', line: '帮我拿一下包嘛，我系个鞋带。', context: '第一次约会 · 散步途中', aside: '你手上没东西，也愿意帮忙。这个时候，你会怎么做？', choices: stories[1]!.scenes[1]!.choices.map(c => ({...c, next: undefined})),
}
