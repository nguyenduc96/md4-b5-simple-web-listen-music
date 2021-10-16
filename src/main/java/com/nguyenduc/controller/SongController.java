package com.nguyenduc.controller;

import com.nguyenduc.model.Song;
import com.nguyenduc.model.SongForm;
import com.nguyenduc.service.song.ISongService;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Controller
public class SongController {
    @Value("${file-upload}")
    private String fileUpload;

    @Autowired
    private ISongService songService;

    @GetMapping("/home")
    public ModelAndView home(@RequestParam(name = "q", required = false) String q) {
        ModelAndView modelAndView = new ModelAndView("index");
        List<Song> songList;
        if (q == null || q.equals("")) {
            songList = songService.findAll();
        } else {
            songList = songService.findByName(q);
        }
        modelAndView.addObject("songList", songList);
        return modelAndView;
    }

    @GetMapping("/create")
    public ModelAndView createForm() {
        return new ModelAndView("create", "songForm", new SongForm());
    }

    @PostMapping("/save")
    public String createSong(@ModelAttribute SongForm songForm, Model model) throws IOException {
        String fileName = convertMultipartFileToString(songForm);
        Song song = new Song(songForm.getId(), songForm.getName(), songForm.getSinger(), songForm.getCategory(), fileName);
        songService.save(song);
        if (songForm.getId() == null) {
            model.addAttribute("message", "Add new song successfully!!!");
        } else {
            model.addAttribute("message", "Update info song successfully!!!");
        }
        return "create";
    }

    private String convertMultipartFileToString(SongForm songForm) throws IOException {
        MultipartFile multipartFile = songForm.getLink();
        String fileName = multipartFile.getOriginalFilename();
        FileCopyUtils.copy(songForm.getLink().getBytes(), new File(fileUpload + fileName));
        return fileName;
    }

    @GetMapping("/{id}/edit")
    public ModelAndView formUpdate(@PathVariable("id") Long id) throws IOException {
        Song song = songService.findById(id);
        File file = new File(song.getLink());
        DiskFileItem fileItem = new DiskFileItem("file", "text/plain", false, file.getName(), (int) file.length() , file.getParentFile());
        fileItem.getOutputStream();
        MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
        SongForm songForm = new SongForm(
                song.getId(), song.getName(), song.getSinger(), song.getCategory(),
                multipartFile
        );
        ModelAndView modelAndView = new ModelAndView("edit");
        modelAndView.addObject("songForm", songForm);
        return modelAndView;
    }

    @GetMapping("/{id}/delete")
    public ModelAndView formDelete(@PathVariable("id") Long id) throws IOException {
        Song song = songService.findById(id);
        ModelAndView modelAndView = new ModelAndView("delete");
        modelAndView.addObject("song", song);
        return modelAndView;
    }

    @PostMapping("/delete")
    public String delete(@ModelAttribute Song song) {
        songService.remove(song.getId());
        return "redirect:/home";
    }
}
