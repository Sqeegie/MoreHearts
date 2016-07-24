# More Hearts
Source to http://dev.bukkit.org/bukkit-plugins/more-hearts/. Originally created by roei12. 


Known Bugs:
- Using a healing command such as /heal will throw off the health amount saved on file. This is applicable when adding more hearts. The current health will drop back down to the amount of health before the heal command was executed.
- When adding/setting hearts. Exceptions will get thrown when the addition value is a string or negative number.