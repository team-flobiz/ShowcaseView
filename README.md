# ShowcaseView
<img width="370" src="https://github.com/team-flobiz/ShowcaseView/assets/92913644/d77dbc17-f02d-49bc-80a1-169cec53f070">

Create Showcase Instance

```private val showcase by lazy { Showcase(this) }```

To showcase on any anchor, add ```ShowcaseTarget``` to ```showcase```. Library allows to add multiple anchor targets to showcase as a sequence:

**ShowcaseTarget.Sync**: For static anchor target views on screen.

**ShowcaseTarget.Async**: For async views where target view can be added with target finder predicate.

**ShowcaseTarget.ViewHolderItem**: For showcaseing RecyclerView Item, `predicate` allows to find desired ViewHolder and `targetFinder` to find anchor view in ViewHolder.

```
ShowcaseTarget.Sync(
    id = anchor.id.toString(),
    style = ShowcaseTarget.Style(
        hint = "ShowcaseView demo",
        shape = ShowcaseShape.RoundedRectangle(4.dp),
        dismissAction = "Continue"
    ),
    target = anchor,
).run(showcase::add)
```

**Styling:**
Style of showcase can be determined with `style` attribute for each `ShowcaseTarget`.

```
Style(
    val hint: String? = null,
    /* if null -> overlay become clickable to dismiss */
    val dismissAction: String? = null,
    val shape: ShowcaseShape = ShowcaseShape.Rectangle,
    val gravity: ShowcaseGravity = ShowcaseGravity.Bottom,
    val hintMargin: Int? = null,
    val actionMargin: Int? = null,
    /* provide negative value for inset padding */
    val padding: Int? = 8,
    val hasCloseButton: Boolean = false
)
```
